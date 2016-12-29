package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.cache.NewsArticleCache;
import fi.otavanopisto.kuntaapi.server.cache.NewsArticleImageCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Post;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private NewsArticleCache newsArticleCache;
  
  @Inject
  private NewsArticleImageCache newsArticleImageCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<NewsArticleIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-news";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes NewsArticleIdUpdateRequest event) {
    if (!stopped) {
      NewsArticleId newsArticleId = event.getId();
      
      if (!StringUtils.equals(newsArticleId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event);
        queue.add(0, event);
      } else {
        if (!queue.contains(event)) {
          queue.add(event);
        }
      }
    }
  }
  
  @Asynchronous
  public void onNewsArticleIdRemoveRequest(@Observes NewsArticleIdRemoveRequest event) {
    if (!stopped) {
      NewsArticleId newsArticleId = event.getId();
      
      if (!StringUtils.equals(newsArticleId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteNewsArticle(event, newsArticleId);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        NewsArticleIdUpdateRequest updateRequest = queue.remove(0);
        updateManagementPost(updateRequest.getOrganizationId(), updateRequest.getId());
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementPost(OrganizationId organizationId, NewsArticleId newsArticleId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Post> response = api.wpV2PostsIdGet(newsArticleId.getId(), null, null);
    if (response.isOk()) {
      updateManagementPost(organizationId, api, response.getResponse());
    } else {
      logger.warning(String.format("Find organization %s post %s failed on [%d] %s", organizationId.getId(), newsArticleId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPost(OrganizationId organizationId, DefaultApi api, Post managementPost) {
    NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPost.getId()));

    Identifier identifier = identifierController.findIdentifierById(newsArticleId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(newsArticleId);
    }
    
    NewsArticleId kuntaApiNewsArticleId = new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    NewsArticle newsArticle = managementTranslator.translateNewsArticle(kuntaApiNewsArticleId, managementPost);
    if (newsArticle == null) {
      logger.severe(String.format("Failed to translate news article %d", managementPost.getId()));
      return;
    }
    
    newsArticleCache.put(kuntaApiNewsArticleId, newsArticle);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(newsArticle));
    
    if (managementPost.getFeaturedMedia() != null && managementPost.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, kuntaApiNewsArticleId, api, managementPost.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, NewsArticleId newsArticleId, DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAttachment.getId()));
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(managementAttachmentId);
      }
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment);
      if (attachment == null) {
        logger.severe(String.format("Failed to translate news article attachment %d", featuredMedia));
        return;
      }
      
      newsArticleImageCache.put(new IdPair<>(newsArticleId, kuntaApiAttachmentId), attachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
    }
  }

  private void deleteNewsArticle(NewsArticleIdRemoveRequest event, NewsArticleId newsArticleId) {
    OrganizationId organizationId = event.getOrganizationId();
    
    Identifier newsArticleIdentifier = identifierController.findIdentifierById(newsArticleId);
    if (newsArticleIdentifier != null) {
      NewsArticleId kuntaApiNewsArticleId = new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newsArticleIdentifier.getKuntaApiId());
      queue.remove(new NewsArticleIdUpdateRequest(organizationId, kuntaApiNewsArticleId, false));

      modificationHashCache.clear(newsArticleIdentifier.getKuntaApiId());
      newsArticleCache.clear(kuntaApiNewsArticleId);
      identifierController.deleteIdentifier(newsArticleIdentifier);
      
      List<IdPair<NewsArticleId,AttachmentId>> newsArticleImageIds = newsArticleImageCache.getChildIds(kuntaApiNewsArticleId);
      for (IdPair<NewsArticleId,AttachmentId> newsArticleImageId : newsArticleImageIds) {
        AttachmentId attachmentId = newsArticleImageId.getChild();
        newsArticleImageCache.clear(newsArticleImageId);
        modificationHashCache.clear(attachmentId.getId());
        
        Identifier imageIdentifier = identifierController.findIdentifierById(attachmentId);
        if (imageIdentifier != null) {
          identifierController.deleteIdentifier(imageIdentifier);
        }
      }
    }
  }

}
