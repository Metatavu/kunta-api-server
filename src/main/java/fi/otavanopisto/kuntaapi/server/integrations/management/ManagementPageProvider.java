package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.PageCache;
import fi.otavanopisto.kuntaapi.server.cache.PageContentCache;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;

/**
 * Page provider for management service
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementPageProvider extends AbstractManagementProvider implements PageProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private PageCache pageCache;
  
  @Inject
  private PageContentCache pageContentCache;
  
  @Inject
  private ManagementImageLoader managementImageLoader;

  @Inject
  private IdController idController;
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    return listPages(organizationId, parentId, onlyRootPages);
  }

  @Override
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return pageCache.get(pageId);
  }
  
  @Override
  @SuppressWarnings ("squid:S1168")
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    return pageContentCache.get(pageId);
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page managementPage = findPageByPageId(organizationId, pageId);
    if (managementPage != null) {
      Integer featuredMediaId = managementPage.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(organizationId, featuredMedia));
        }
      }
    }
  
    return Collections.emptyList();
  }

  @Override
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    fi.otavanopisto.mwp.client.model.Page page = findPageByPageId(organizationId, pageId);
    if (page != null) {
      Integer featuredMediaId = page.getFeaturedMedia();
      if (featuredMediaId != null) {
        AttachmentId managementAttachmentId = getImageAttachmentId(organizationId, featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(organizationId, attachment);
        }
      }
    }
  
    return null;
  }

  @Override
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId,
      Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
    if (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE) {
      AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
      
      if (size != null) {
        return scaleImage(imageData, size);
      } else {
        return imageData;
      }
      
    }
    
    return null;
  }
  
  private fi.otavanopisto.mwp.client.model.Page findPageByPageId(OrganizationId organizationId, PageId pageId) {
    PageId managementPageId = idController.translatePageId(pageId, ManagementConsts.IDENTIFIER_NAME);
    if (managementPageId == null) {
      logger.severe(String.format("Failed to convert %s into management page id", pageId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Page> response = managementApi.getApi(organizationId).wpV2PagesIdGet(managementPageId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding page failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

  private List<Page> listPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    List<PageId> pageIds = pageCache.getOragnizationIds(organizationId);
    List<Page> result = new ArrayList<>(pageIds.size());
    PageId kuntaApiParentId = null;
    if (parentId != null) {
      kuntaApiParentId = idController.translatePageId(parentId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentId == null) {
        logger.severe(String.format("Could not translate page %s into Kunta API page id", parentId.getId()));
        return Collections.emptyList();
      }
    }
    
    for (PageId pageId : pageIds) {
      Page page = pageCache.get(pageId);
      if ((page != null) && isAcceptablePage(organizationId, page, onlyRootPages, kuntaApiParentId)) {
        result.add(page);
      }
    }
    
    return result;
  }
  
  private boolean isAcceptablePage(OrganizationId organizationId, Page page, boolean onlyRootPages, PageId kuntaApiParentId) {
    PageId pageParentId = page.getParentId() != null ? new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getParentId()) : null;
    if (onlyRootPages) {
      return pageParentId == null;
    } else {
      if (kuntaApiParentId != null) {
        return idController.idsEqual(kuntaApiParentId, pageParentId);
      }      
    }
    
    return true;
  }

}
