package fi.metatavu.kuntaapi.server.integrations.management;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.ejb3.annotation.Pool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdMapController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.index.IndexRemoveRequest;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexablePage;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementPageContentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementPageResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.PageIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Page;
import fi.metatavu.management.client.model.Tag;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = PageIdTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.HIGH_CONCURRENCY_POOL)
public class ManagementPageEntityDiscoverJob extends AbstractJmsJob<IdTask<PageId>> {
  
  private static final String SITE_ROOT_TAG = "siteroot";
  private static final String HIDE_MENU_CHILDREN_TAG = "hidemenuchildren";

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 

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
  private ManagementPageResourceContainer managementPageResourceContainer;

  @Inject
  private ManagementAttachmentDataResourceContainer managementAttachmentDataResourceContainer;

  @Inject
  private ManagementPageContentResourceContainer managementPageContentResourceContainer;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private ScaledImageStore scaledImageStore;

  @Override
  public void execute(IdTask<PageId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementPage(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementPage(task.getId());
    }
  }
  
  private void updateManagementPage(PageId managementPageId, Long orderIndex) {
    OrganizationId organizationId = managementPageId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Page> response = api.wpV2PagesIdGet(managementPageId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementPage(organizationId, api, response.getResponse(), orderIndex);
    } else {
      logger.warning(() -> String.format("Find organization %s page %s failed on [%d] %s", organizationId.getId(), managementPageId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPage(OrganizationId organizationId, DefaultApi api, Page managementPage, Long orderIndex) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format("Failed to translate organization into KuntaAPI id %s", organizationId));
      return;
    }
    
    List<String> tags = new ArrayList<>(getPageTags(api, managementPage.getId()));
    PageId managementPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));
    
    BaseId mappedParentId = idMapController.findMappedPageParentId(organizationId, managementPageId);
    String unmappedParentId = null;
    boolean hasParent = managementPage.getParent() != null && managementPage.getParent() > 0;
    BaseId kuntaApiParentId = null;
    
    if (hasParent) {
      PageId managementParentPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getParent()));
      kuntaApiParentId = idController.translatePageId(managementParentPageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentId == null) {
        logger.severe(() -> String.format("Could not translate %d parent page %d into management page id", managementPage.getParent(), managementPage.getId()));
        return;
      }
    }
    
    BaseId identifierParentId;
    if (mappedParentId != null) {
      unmappedParentId = kuntaApiParentId != null ? kuntaApiParentId.getId() : "ROOT";
      identifierParentId = mappedParentId;
    } else {
      identifierParentId = kuntaApiParentId == null ? organizationId : kuntaApiParentId;
    }

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementPageId);
    identifierRelationController.setParentId(identifier, identifierParentId);
    
    PageId pageParentId = identifierParentId instanceof PageId ? (PageId) identifierParentId : null;
    PageId kuntaApiPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Boolean siteRootPage = tags.remove(SITE_ROOT_TAG);
    Boolean hideMenuChildren = tags.remove(HIDE_MENU_CHILDREN_TAG);
    
    fi.metatavu.kuntaapi.server.rest.model.Page page = managementTranslator.translatePage(kuntaApiPageId, pageParentId, unmappedParentId, siteRootPage, hideMenuChildren, managementPage);
    String title = managementPage.getTitle().getRendered();
    String processedHtml = processPage(api, kuntaApiOrganizationId, identifier, managementPage);
    
    List<LocalizedValue> pageContents = managementTranslator.translateLocalized(processedHtml);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementPage));
    managementPageResourceContainer.put(kuntaApiPageId, page);
    managementPageContentResourceContainer.put(kuntaApiPageId, pageContents);
    indexRequest.fire(new IndexRequest(createIndexablePage(organizationId, kuntaApiPageId, processedHtml, title, orderIndex, managementPage.getMenuOrder(), pageParentId, tags)));
  }
  
  /**
   * Lists tags for a page
   * 
   * @param api Wordpress API instance
   * @param pageId page id
   * @return list of tags
   */
  private List<String> getPageTags(DefaultApi api, Integer pageId) {
    ApiResponse<List<Tag>> tagsResponse = api.wpV2TagsGet(null, null, null, null, null, null, null, null, null, false, pageId, null);
    if (tagsResponse.isOk()) {
      return tagsResponse.getResponse().stream().map(Tag::getName).collect(Collectors.toList());
    } else {
      logger.warning(() -> String.format("List page %d tags failed on [%d] %s", pageId, tagsResponse.getStatus(), tagsResponse.getMessage()));
    }
    
    return Collections.emptyList();
  }

  private String processPage(DefaultApi api, OrganizationId kuntaApiOrganizationId, Identifier pageIdentifier, Page managementPage) {
    String originalHtml = managementPage.getContent().getRendered();
    
    String baseUrl = organizationSettingController.getSettingValue(kuntaApiOrganizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
    boolean changed = false;
    Document document = Jsoup.parse(originalHtml);
    Elements images = document.select("img[class*=\"wp-image-\"]");
    long orderIndex = 3l;
    List<AttachmentId> contentKuntaApiAttachmentIds = new ArrayList<>();
    
    for (Element image : images) {
      Long mediaId = extractMediaId(image, baseUrl);
      if (mediaId != null) {
        AttachmentId kuntaApiAttachmentId = updateAttachment(kuntaApiOrganizationId, pageIdentifier, api, mediaId, ManagementConsts.ATTACHMENT_TYPE_PAGE_CONTENT_IMAGE, orderIndex);
        if (kuntaApiAttachmentId != null) {
          contentKuntaApiAttachmentIds.add(kuntaApiAttachmentId);
          image.attr("src", "about:blank");
          image.attr("data-organization-id", kuntaApiOrganizationId.getId());
          image.attr("data-page-id", pageIdentifier.getKuntaApiId());
          image.attr("data-attachment-id", kuntaApiAttachmentId.getId());
          image.attr("data-image-type", ManagementConsts.ATTACHMENT_TYPE_PAGE_CONTENT_IMAGE);
          image.addClass("kunta-api-image");
          image.removeAttr("srcset");
          image.removeAttr("sizes");
          changed = true;
        }
      }
    }

    updateEmbeddedServices(pageIdentifier, document);
    updateEmbeddedServiceLocationServiceChannels(pageIdentifier, document);
    
    String result = changed ? document.body().html() : originalHtml;
    updateAttachments(kuntaApiOrganizationId, api, managementPage, pageIdentifier, contentKuntaApiAttachmentIds);
    
    return result;
  }
  
  private void updateEmbeddedServices(Identifier pageIdentifier, Document document) {
    Set<String> embeddedServiceIds = new HashSet<>();
    Elements embeddedServiceArticles = document.select("article[data-type=\"kunta-api-service-component\"]");
    for (Element embeddedServiceArticle : embeddedServiceArticles) {
      String embeddedServiceId = embeddedServiceArticle.attr("data-service-id"); 
      if (StringUtils.isNotBlank(embeddedServiceId)) {
        embeddedServiceIds.add(embeddedServiceId); 
      }
    }
    
    updateEmbeddedServiceIds(pageIdentifier, embeddedServiceIds);
  }

  private void updateEmbeddedServiceIds(Identifier pageIdentifier, Set<String> embeddedServiceIds) {
    PageId kuntaApiPageId = kuntaApiIdFactory.createFromIdentifier(PageId.class, pageIdentifier);
    List<ServiceId> existingServiceIds = identifierRelationController.listServiceIdsByParentId(kuntaApiPageId);
    for (String embeddedServiceId : embeddedServiceIds) {
      ServiceId kuntaApiServiceId = kuntaApiIdFactory.createServiceId(embeddedServiceId);
      Identifier serviceIdentifier = identifierController.findIdentifierById(kuntaApiServiceId);
      if (serviceIdentifier != null) {
        existingServiceIds.remove(kuntaApiServiceId);
        identifierRelationController.addChild(pageIdentifier, serviceIdentifier);
      }
    }
    
    for (ServiceId serviceId : existingServiceIds) {
      identifierRelationController.removeChild(kuntaApiPageId, serviceId);
    }
  }

  private void updateEmbeddedServiceLocationServiceChannels(Identifier pageIdentifier, Document document) {
    Set<String> embeddedServiceLocationServiceChannelIds = new HashSet<>();
    Elements embeddedServiceLocationChannelArticles = document.select("article[data-type=\"kunta-api-service-location-component\"]");
    for (Element embeddedServiceLocationChannelArticle : embeddedServiceLocationChannelArticles) {
      String embeddedServiceChannelId = embeddedServiceLocationChannelArticle.attr("data-service-channel-id"); 
      if (StringUtils.isNotBlank(embeddedServiceChannelId)) {
        embeddedServiceLocationServiceChannelIds.add(embeddedServiceChannelId); 
      }
    }
    
    updateEmbeddedServiceLocationServiceChannelIds(pageIdentifier, embeddedServiceLocationServiceChannelIds);
  }

  private void updateEmbeddedServiceLocationServiceChannelIds(Identifier pageIdentifier, Set<String> embeddedServiceLocationServiceChannelIds) {
    PageId kuntaApiPageId = kuntaApiIdFactory.createFromIdentifier(PageId.class, pageIdentifier);
    List<ServiceLocationServiceChannelId> serviceLocationServiceChannelIds = identifierRelationController.listServiceLocationServiceChannelIdsByParentId(kuntaApiPageId);
    for (String embeddedServiceLocationServiceChannelId : embeddedServiceLocationServiceChannelIds) {
      ServiceLocationServiceChannelId serviceLocationServiceChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(embeddedServiceLocationServiceChannelId);
      Identifier serviceLocationServiceChannelIdentifier = identifierController.findIdentifierById(serviceLocationServiceChannelId);
      if (serviceLocationServiceChannelIdentifier != null) {
        serviceLocationServiceChannelIds.remove(serviceLocationServiceChannelId);
        identifierRelationController.addChild(pageIdentifier, serviceLocationServiceChannelIdentifier);
      }
    }
    
    for (ServiceLocationServiceChannelId serviceLocationServiceChannelId : serviceLocationServiceChannelIds) {
      identifierRelationController.removeChild(kuntaApiPageId, serviceLocationServiceChannelId);
    }
  }
    
  private Long extractMediaId(Element image, String managementUrl) {
    URI imageUri = createURI(image.absUrl("src"));
    if (imageUri == null) {
      return null;
    }

    URI managementUri = createURI(managementUrl);
    if (managementUri == null) {
      return null;
    }
    
    if (StringUtils.equals(managementUri.getHost(), imageUri.getHost())) {
      Set<String> classNames = image.classNames();
      for (String className : classNames) {
        if (StringUtils.startsWith(className, "wp-image-")) {
          return NumberUtils.createLong(className.substring(9));
        }
      }
    }
    
    return null;
  }
  
  private URI createURI(String str) {
    try {
      return URI.create(str);
    } catch (Exception e) {
      if (logger.isLoggable(Level.WARNING)) {
        logger.log(Level.WARNING, String.format("Failed to parse URI from %s", str), e);
      }
    }
    
    return null;
  }

  private void updateAttachments(OrganizationId organizationId, DefaultApi api, Page managementPage, Identifier pageIdentifier, List<AttachmentId> contentImageKuntaApiAttachmentIds) {
    PageId pageKuntaApiId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, pageIdentifier.getKuntaApiId());
    List<AttachmentId> existingAttachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, pageKuntaApiId);
    for (AttachmentId contentImageAttachmentId : contentImageKuntaApiAttachmentIds) {
      existingAttachmentIds.remove(contentImageAttachmentId);
    }
    
    if (managementPage.getFeaturedMedia() != null && managementPage.getFeaturedMedia() > 0) {
      AttachmentId attachmentId = updateAttachment(organizationId, pageIdentifier, api, managementPage.getFeaturedMedia().longValue(), ManagementConsts.ATTACHMENT_TYPE_PAGE_FEATURED, 0l);
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }
    
    if (managementPage.getBannerImage() != null && managementPage.getBannerImage() > 0) {
      AttachmentId attachmentId = updateAttachment(organizationId, pageIdentifier, api, managementPage.getBannerImage(), ManagementConsts.ATTACHMENT_TYPE_PAGE_BANNER, 1l); 
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }
    
    for (AttachmentId existingAttachmentId : existingAttachmentIds) {
      identifierRelationController.removeChild(pageKuntaApiId, existingAttachmentId);
    }
  }
  
  private AttachmentId updateAttachment(OrganizationId organizationId, Identifier pageIdentifier, DefaultApi api, Long managementMediaId, String type, Long orderIndex) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(managementMediaId), null, null);
    if (!response.isOk()) {
      logger.severe(() -> String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      fi.metatavu.management.client.model.Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = managementTranslator.createManagementAttachmentId(organizationId, managementAttachment.getId(), type);
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementAttachmentId);
      identifierRelationController.addChild(pageIdentifier, identifier);
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      Attachment kuntaApiAttachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, type);
      managementAttachmentResourceContainer.put(kuntaApiAttachmentId, kuntaApiAttachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        if (!dataHash.equals(modificationHashCache.get(identifier.getKuntaApiId()))) {
          modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
          managementAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
          scaledImageStore.purgeStoredImages(kuntaApiAttachmentId);
        }
      }
      
      return kuntaApiAttachmentId;
    }
  }

  private void deleteManagementPage(PageId managementPageId) {
    OrganizationId organizationId = managementPageId.getOrganizationId();
    
    Identifier pageIdentifier = identifierController.findIdentifierById(managementPageId);
    if (pageIdentifier != null) {
      PageId kuntaApiPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, pageIdentifier.getKuntaApiId());
      modificationHashCache.clear(pageIdentifier.getKuntaApiId());
      managementPageResourceContainer.clear(kuntaApiPageId);
      managementPageContentResourceContainer.clear(kuntaApiPageId);
      identifierController.deleteIdentifier(pageIdentifier);
      
      IndexablePage indexRemove = new IndexablePage();
      indexRemove.setPageId(kuntaApiPageId.getId());
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }

  /**
   * Creates a page model for indexing
   * 
   * @param organizationId organization id
   * @param kuntaApiPageId pageId in Kunta API format
   * @param content page content
   * @param title page title
   * @param orderIndex page order index 
   * @param menuOrder page menu order
   * @param pageParentId page parent id
   * @param tags 
   * @return indexable page
   */
  @SuppressWarnings ("squid:S00107")
  private IndexablePage createIndexablePage(OrganizationId organizationId, PageId kuntaApiPageId, String content, String title, Long orderIndex, Integer menuOrder, PageId pageParentId, List<String> tags) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(kuntaApiPageId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(() -> String.format("Failed to translate organizationId %s into KuntaAPI id", organizationId.toString()));
      return null;
    }
    
    IndexablePage indexablePage = new IndexablePage();
    indexablePage.setTitleRaw(title);
    indexablePage.setContentFi(unescapeString(content));
    indexablePage.setOrganizationId(kuntaApiOrganizationId.getId());
    indexablePage.setPageId(kuntaApiPageId.getId());
    indexablePage.setParentId(pageParentId != null ? pageParentId.getId() : null);
    indexablePage.setTitleFi(unescapeString(title));
    indexablePage.setOrderIndex(orderIndex);
    indexablePage.setOrderNumber(menuOrder);
    indexablePage.setMenuOrder(menuOrder);
    indexablePage.setTags(tags);
    
    return indexablePage;
  }

  /**
   * Unescapes HTML entities from string and removes soft hyphens from string
   * 
   * @param string string
   * @return unescaped string
   */
  private String unescapeString(String string) {
    if (StringUtils.isEmpty(string)) {
      return null;
    }
    
    return StringEscapeUtils.unescapeHtml4(string).replaceAll("\u00AD", "");
  }
}
