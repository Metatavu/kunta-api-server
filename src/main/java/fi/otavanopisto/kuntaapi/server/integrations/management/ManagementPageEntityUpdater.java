package fi.otavanopisto.kuntaapi.server.integrations.management;

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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Page;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdMapController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.PageIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.PageIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemovePage;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexablePage;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageContentCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageImageCache;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

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
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private IdMapController idMapController;

  @Inject
  private ManagementPageCache pageCache;
  
  @Inject
  private ManagementPageContentCache pageContentCache;
  
  @Inject
  private ManagementPageImageCache pageImageCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<PageIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(ManagementConsts.IDENTIFIER_NAME);
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
  public void onPageIdUpdateRequest(@Observes PageIdUpdateRequest event) {
    if (!stopped) {
      PageId pageId = event.getId();
      
      if (!StringUtils.equals(pageId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onPageIdRemoveRequest(@Observes PageIdRemoveRequest event) {
    if (!stopped) {
      PageId pageId = event.getId();
      
      if (!StringUtils.equals(pageId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deletePage(event, pageId);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        PageIdUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateManagementPage(updateRequest);
        }
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementPage(PageIdUpdateRequest updateRequest) {
    OrganizationId organizationId = updateRequest.getOrganizationId();
    PageId pageId = updateRequest.getId();
    DefaultApi api = managementApi.getApi(organizationId);
    Long orderIndex = updateRequest.getOrderIndex();
    
    ApiResponse<Page> response = api.wpV2PagesIdGet(pageId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementPage(organizationId, api, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s page %s failed on [%d] %s", organizationId.getId(), pageId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPage(OrganizationId organizationId, DefaultApi api, Page managementPage, Long orderIndex) {
    PageId managementPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));
    
    BaseId identifierParentId = idMapController.findMappedPageParentId(organizationId, managementPageId);

    if (identifierParentId == null && managementPage.getParent() != null && managementPage.getParent() > 0) {
      PageId managementParentPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME,String.valueOf(managementPage.getParent()));
      identifierParentId = idController.translatePageId(managementParentPageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (identifierParentId == null) {
        logger.severe(String.format("Could not translate %d parent page %d into management page id", managementPage.getParent(), managementPage.getId()));
        return;
      } 
    }
    
    if (identifierParentId == null) {
      identifierParentId = organizationId;
    }
    
    Identifier identifier = identifierController.findIdentifierById(managementPageId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, managementPageId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }
    
    identifierRelationController.setParentId(identifier, identifierParentId);
    
    PageId pageParentId = identifierParentId instanceof PageId ? (PageId) identifierParentId : null;
    PageId kuntaApiPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    
    fi.metatavu.kuntaapi.server.rest.model.Page page = managementTranslator.translatePage(kuntaApiPageId, pageParentId, managementPage);
    String contents = managementPage.getContent().getRendered();
    String title = managementPage.getTitle().getRendered();
    List<LocalizedValue> pageContents = managementTranslator.translateLocalized(contents);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementPage));
    pageCache.put(kuntaApiPageId, page);
    pageContentCache.put(kuntaApiPageId, pageContents);
    indexRequest.fire(new IndexRequest(createIndexablePage(organizationId, kuntaApiPageId, ManagementConsts.DEFAULT_LOCALE, contents, title)));

    updateAttachments(organizationId, api, managementPage, kuntaApiPageId);
  }

  private void updateAttachments(OrganizationId organizationId, DefaultApi api, Page managementPage,
      PageId kuntaApiPageId) {
    if (managementPage.getFeaturedMedia() != null && managementPage.getFeaturedMedia() > 0) {
      updateAttachment(organizationId, kuntaApiPageId, api, managementPage.getFeaturedMedia().longValue(), ManagementConsts.ATTACHMENT_TYPE_PAGE_FEATURED); 
    }
    
    if (managementPage.getBannerImage() != null && managementPage.getBannerImage() > 0) {
      updateAttachment(organizationId, kuntaApiPageId, api, managementPage.getBannerImage(), ManagementConsts.ATTACHMENT_TYPE_PAGE_BANNER); 
    }
  }
  
  private void updateAttachment(OrganizationId organizationId, PageId pageId, DefaultApi api, Long managementMediaId, String type) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(managementMediaId), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      fi.metatavu.management.client.model.Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAttachment.getId()));
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(orderIndex, managementAttachmentId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
      
      identifierRelationController.addChild(pageId, identifier);
      
      // FIXME: Remove page image cache
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      Attachment kuntaApiAttachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, type);
      pageImageCache.put(new IdPair<PageId, AttachmentId>(pageId, kuntaApiAttachmentId), kuntaApiAttachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
    }
  }

  private void deletePage(PageIdRemoveRequest event, PageId managementPageId) {
    OrganizationId organizationId = event.getOrganizationId();
    
    Identifier pageIdentifier = identifierController.findIdentifierById(managementPageId);
    if (pageIdentifier != null) {
      PageId kuntaApiPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, pageIdentifier.getKuntaApiId());
      queue.remove(managementPageId);
      modificationHashCache.clear(pageIdentifier.getKuntaApiId());
      pageCache.clear(kuntaApiPageId);
      pageContentCache.clear(kuntaApiPageId);
      identifierController.deleteIdentifier(pageIdentifier);
      
      IndexRemovePage indexRemove = new IndexRemovePage();
      indexRemove.setPageId(kuntaApiPageId.getId());
      indexRemove.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
      
      List<IdPair<PageId,AttachmentId>> pageImageIds = pageImageCache.getChildIds(kuntaApiPageId);
      for (IdPair<PageId,AttachmentId> pageImageId : pageImageIds) {
        AttachmentId attachmentId = pageImageId.getChild();
        pageImageCache.clear(pageImageId);
        modificationHashCache.clear(attachmentId.getId());
        
        Identifier imageIdentifier = identifierController.findIdentifierById(attachmentId);
        if (imageIdentifier != null) {
          identifierController.deleteIdentifier(imageIdentifier);
        }
      }
    }
  }

  private IndexablePage createIndexablePage(OrganizationId organizationId, PageId kuntaApiPageId, String language, String content, String title) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(kuntaApiPageId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(String.format("Failed to translate organizationId %s into KuntaAPI id", organizationId.toString()));
      return null;
    }
    
    IndexablePage indexablePage = new IndexablePage();
    indexablePage.setContent(content);
    indexablePage.setLanguage(language);
    indexablePage.setOrganizationId(kuntaApiOrganizationId.getId());
    indexablePage.setPageId(kuntaApiPageId.getId());
    indexablePage.setTitle(title);
    
    return indexablePage;
  }
  
}
