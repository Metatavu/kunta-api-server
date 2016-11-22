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
import fi.otavanopisto.kuntaapi.server.cache.PageCache;
import fi.otavanopisto.kuntaapi.server.cache.PageContentCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.PageIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
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

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private PageCache pageCache;
  
  @Inject
  private PageContentCache pageContentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<PageIdUpdateRequest> queue;

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
  public void onOrganizationIdUpdateRequest(@Observes PageIdUpdateRequest event) {
    if (!stopped) {
      PageId pageId = event.getId();
      
      if (!StringUtils.equals(pageId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
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
        PageIdUpdateRequest updateRequest = queue.remove(0);
        updateManagementPage(updateRequest.getOrganizationId(), updateRequest.getId());
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementPage(OrganizationId organizationId, PageId pageId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Page> response = api.wpV2PagesIdGet(pageId.getId(), null);
    if (response.isOk()) {
      updateManagementPage(organizationId, api, response.getResponse());
    } else {
      logger.warning(String.format("Find organization %s page %s failed on [%d] %s", organizationId.getId(), pageId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPage(OrganizationId organizationId, DefaultApi api, Page managementPage) {
    PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));

    Identifier identifier = identifierController.findIdentifierById(pageId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(pageId);
    }
    
    PageId kuntaApiPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.otavanopisto.kuntaapi.server.rest.model.Page page = translatePage(organizationId, kuntaApiPageId, managementPage);
    List<LocalizedValue> pageContents = translateLocalized(managementPage.getContent().getRendered());
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(page));
    pageCache.put(kuntaApiPageId, page);
    pageContentCache.put(kuntaApiPageId, pageContents);
    
    if (managementPage.getFeaturedMedia() != null && managementPage.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, api, managementPage.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.otavanopisto.mwp.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment attachment = response.getResponse();
      AttachmentId attachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(attachment.getId()));
      
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
  
  private fi.otavanopisto.kuntaapi.server.rest.model.Page translatePage(OrganizationId organizationId, PageId kuntaApiPageId, fi.otavanopisto.mwp.client.model.Page managementPage) {
    fi.otavanopisto.kuntaapi.server.rest.model.Page page = new fi.otavanopisto.kuntaapi.server.rest.model.Page();
    PageId kuntaApiParentPageId = null;
    
    if (managementPage.getParent() != null && managementPage.getParent() > 0) {
      PageId managementParentPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME,String.valueOf(managementPage.getParent()));
      kuntaApiParentPageId = idController.translatePageId(managementParentPageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentPageId == null) {
        logger.severe(String.format("Could not translate %d parent page %d into management page id", managementPage.getParent(), managementPage.getId()));
        return null;
      } 
    }
    
    page.setTitles(translateLocalized(managementPage.getTitle().getRendered()));
    
    page.setId(kuntaApiPageId.getId());
    
    if (kuntaApiParentPageId != null) {
      page.setParentId(kuntaApiParentPageId.getId());
    }
    
    page.setSlug(managementPage.getSlug());
    
    return page;
  }
  
  private List<LocalizedValue> translateLocalized(String value) {
    List<LocalizedValue> result = new ArrayList<>();
    
    if (StringUtils.isNotBlank(value)) {
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      localizedValue.setValue(value);
      result.add(localizedValue);
    }
    
    return result;
  }
}
