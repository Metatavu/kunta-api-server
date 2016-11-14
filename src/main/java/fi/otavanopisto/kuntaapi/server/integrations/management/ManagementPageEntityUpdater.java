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

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.MwpConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.DefaultApi;
import fi.otavanopisto.mwp.client.model.Attachment;
import fi.otavanopisto.mwp.client.model.Page;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-pages";
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
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, MwpConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateManagementPages(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementPages(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<Page> managementPages = listManagementPages(api, organizationId);
    for (Page managementPage : managementPages) {
      updateManagementPage(api, managementPage);
    }
  }

  private List<Page> listManagementPages(DefaultApi api, OrganizationId organizationId) {
    ApiResponse<List<Page>> response = api.wpV2PagesGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s pages failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void updateManagementPage(DefaultApi api, Page managementPage) {
    PageId pageId = new PageId(MwpConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));

    Identifier identifier = identifierController.findIdentifierById(pageId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(pageId);
    }
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementPage));
    
    if (managementPage.getFeaturedMedia() != null && managementPage.getFeaturedMedia() > 0) {
      updateFeaturedMedia(api, managementPage.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.otavanopisto.mwp.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment attachment = response.getResponse();
      AttachmentId attachmentId = new AttachmentId(MwpConsts.IDENTIFIER_NAME, String.valueOf(attachment.getId()));
      
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
