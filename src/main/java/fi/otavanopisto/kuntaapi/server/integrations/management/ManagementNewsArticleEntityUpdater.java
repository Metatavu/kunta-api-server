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
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.DefaultApi;
import fi.otavanopisto.mwp.client.model.Attachment;
import fi.otavanopisto.mwp.client.model.Post;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;
  
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
    
    ApiResponse<Post> response = api.wpV2PostsIdGet(newsArticleId.getId(), null);
    if (response.isOk()) {
      updateManagementPost(api, response.getResponse());
    } else {
      logger.warning(String.format("Find organization %s post %s failed on [%d] %s", organizationId.getId(), newsArticleId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPost(DefaultApi api, Post managementPost) {
    NewsArticleId newsArticleId = new NewsArticleId(ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPost.getId()));

    Identifier identifier = identifierController.findIdentifierById(newsArticleId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(newsArticleId);
    }
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementPost));
    
    if (managementPost.getFeaturedMedia() != null && managementPost.getFeaturedMedia() > 0) {
      updateFeaturedMedia(api, managementPost.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.otavanopisto.mwp.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment attachment = response.getResponse();
      AttachmentId attachmentId = new AttachmentId(ManagementConsts.IDENTIFIER_NAME, String.valueOf(attachment.getId()));
      
      Identifier identifier = identifierController.findIdentifierById(attachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(attachmentId);
      }
      
      AttachmentData imageData = managementImageLoader.getImageData(attachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
    }
  }

}
