package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Post;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.cache.NewsArticleCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementAttachmentCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.NewsArticleIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 5;

  @Inject
  private Logger logger;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private NewsArticleCache newsArticleCache;
  
  @Inject
  private ManagementAttachmentCache managementAttachmentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private NewsArticleIdTaskQueue newsArticleIdTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-news";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void timeout(Timer timer) {
    executeNextTask();
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void executeNextTask() {
    IdTask<NewsArticleId> task = newsArticleIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementPost(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteManagementPost(task.getId());
      }
    }
  }
  
  private void updateManagementPost(NewsArticleId newsArticleId, Long orderIndex) {
    OrganizationId organizationId = newsArticleId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Post> response = api.wpV2PostsIdGet(newsArticleId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementPost(organizationId, api, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s post %s failed on [%d] %s", organizationId.getId(), newsArticleId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPost(OrganizationId organizationId, DefaultApi api, Post managementPost, Long orderIndex) {
    NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPost.getId()));

    Identifier identifier = identifierController.findIdentifierById(newsArticleId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex != null ? orderIndex : Long.MAX_VALUE, newsArticleId);
    } else {
      if (orderIndex != null) {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
    }
    
    identifierRelationController.setParentId(identifier, organizationId);
    
    NewsArticleId newsArticleKuntaApiId = new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    NewsArticle newsArticle = managementTranslator.translateNewsArticle(newsArticleKuntaApiId, managementPost);
    if (newsArticle == null) {
      logger.severe(String.format("Failed to translate news article %d", managementPost.getId()));
      return;
    }
    
    newsArticleCache.put(newsArticleKuntaApiId, newsArticle);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(newsArticle));
    
    List<AttachmentId> existingAttachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, newsArticleKuntaApiId);
    if (managementPost.getFeaturedMedia() != null && managementPost.getFeaturedMedia() > 0) {
      AttachmentId attachmentId = updateFeaturedMedia(organizationId, identifier, api, managementPost.getFeaturedMedia()); 
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }
    
    for (AttachmentId existingAttachmentId : existingAttachmentIds) {
      identifierRelationController.removeChild(newsArticleKuntaApiId, existingAttachmentId);
    }
  }
  
  private AttachmentId updateFeaturedMedia(OrganizationId organizationId, Identifier newsArticleIdentifier, DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = managementTranslator.createManagementAttachmentId(organizationId, managementAttachment.getId(), ManagementConsts.ATTACHMENT_TYPE_NEWS);
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(orderIndex, managementAttachmentId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
      
      identifierRelationController.addChild(newsArticleIdentifier, identifier);

      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, ManagementConsts.ATTACHMENT_TYPE_NEWS);
      if (attachment == null) {
        logger.severe(String.format("Failed to translate news article attachment %d", featuredMedia));
        return null;
      }
      
      managementAttachmentCache.put(kuntaApiAttachmentId, attachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
      
      return kuntaApiAttachmentId;
    }
  }

  private void deleteManagementPost(NewsArticleId managementNewsArticleId) {
    OrganizationId organizationId = managementNewsArticleId.getOrganizationId();
    
    Identifier newsArticleIdentifier = identifierController.findIdentifierById(managementNewsArticleId);
    if (newsArticleIdentifier != null) {
      NewsArticleId kuntaApiNewsArticleId = new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newsArticleIdentifier.getKuntaApiId());
      modificationHashCache.clear(newsArticleIdentifier.getKuntaApiId());
      newsArticleCache.clear(kuntaApiNewsArticleId);
      identifierController.deleteIdentifier(newsArticleIdentifier);
    }
  }

}
