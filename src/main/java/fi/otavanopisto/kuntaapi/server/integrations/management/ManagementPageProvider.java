package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

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
  private ManagementImageLoader managementImageLoader;

  @Inject
  private IdController idController;
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, String path) {
    if (StringUtils.isNotBlank(path)) {
      fi.otavanopisto.mwp.client.model.Page managementPage = findPageByPath(organizationId, path);
      if (managementPage == null) {
        return Collections.emptyList();
      }
      
      if (parentId != null && !idController.idsEqual(parentId, translatePageId(managementPage.getParent()))) {
        return Collections.emptyList();
      }
    
      return Collections.singletonList(translatePage(managementPage));
    } else {
      return listPages(organizationId, parentId, onlyRootPages);
    }
  }

  @Override
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page managementPage = findPageByPageId(organizationId, pageId);
    if (managementPage != null) {
      return translatePage(managementPage);
    }
  
    return null;
  }
  
  @Override
  @SuppressWarnings ("squid:S1168")
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page managementPage = findPageByPageId(organizationId, pageId);
    if (managementPage != null) {
      return translateLocalized(managementPage.getContent().getRendered());
    }
  
    // Returning null to indacate that this provider could not find contents for this page
    
    return null;
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page managementPage = findPageByPageId(organizationId, pageId);
    if (managementPage != null) {
      Integer featuredMediaId = managementPage.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(featuredMedia));
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
        AttachmentId managementAttachmentId = getImageAttachmentId(featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(attachment);
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
  
  private fi.otavanopisto.mwp.client.model.Page findPageByPath(OrganizationId organizationId, String path) {
    fi.otavanopisto.mwp.client.model.Page current = null;
    
    String[] slugs = StringUtils.split(path, "/");
    for (String slug : slugs) {
      if (current == null) {
        current = findPageByParentAndSlug(organizationId, 0, slug);
      } else {
        current = findPageByParentAndSlug(organizationId, current.getId(), slug);
      }
      
      if (current == null) {
        return null;
      }
    }
    
    
    return current;
  }
  
  private fi.otavanopisto.mwp.client.model.Page findPageByParentAndSlug(OrganizationId organizationId, Integer parent, String slug) {
    String context = null;
    Integer page = null;
    Integer perPage = null;
    String search = null;
    LocalDateTime after = null;
    LocalDateTime before = null;
    List<String> include = null;
    Integer offset = null;
    String order = null; 
    String orderby = null;
    String status = null;
    String filter = null;
    List<String> author = null;
    List<String> authorExclude = null;
    Integer menuOrder = null;
    List<String> parentExclude = null;
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Page>> response = managementApi.getApi(organizationId).wpV2PagesGet(
        context, page, perPage, search, after, author, authorExclude, before, authorExclude, include, menuOrder, offset,
        order, orderby, Collections.singletonList(String.valueOf(parent)), parentExclude, slug, status, filter);
    if (!response.isOk()) {
      logger.severe(String.format("Page listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<fi.otavanopisto.mwp.client.model.Page> pages = response.getResponse();
      if (!pages.isEmpty()) {
        return pages.get(0);
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
    String context = null;
    Integer page = null;
    Integer perPage = null;
    String search = null;
    LocalDateTime after = null;
    LocalDateTime before = null;
    List<String> include = null;
    Integer offset = null;
    String order = null; 
    String orderby = null;
    String slug = null;
    String status = null;
    String filter = null;
    List<String> author = null;
    List<String> authorExclude = null;
    Integer menuOrder = null;
    List<String> parent = null;
    List<String> parentExclude = null;
    
    if (onlyRootPages) {
      parent = Collections.singletonList("0"); 
    } else if (parentId != null) {
      PageId managementParentId = idController.translatePageId(parentId, ManagementConsts.IDENTIFIER_NAME);
      if (managementParentId == null) {
        logger.severe(String.format("Could not translate %s into management service id", parentId.toString()));
        return Collections.emptyList();
      }
      
      parent = Collections.singletonList(managementParentId.getId()); 
    }
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Page>> response = managementApi.getApi(organizationId).wpV2PagesGet(
        context, page, perPage, search, after, author, authorExclude, before, authorExclude, include, menuOrder, offset,
        order, orderby, parent, parentExclude, slug, status, filter);
    if (!response.isOk()) {
      logger.severe(String.format("Page listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translatePages(response.getResponse());
    }
    
    return Collections.emptyList();
  }

  private List<Page> translatePages(List<fi.otavanopisto.mwp.client.model.Page> managementPages) {
    List<Page> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Page managementPage : managementPages) {
      result.add(translatePage(managementPage));
    }
    
    return result;
  }

  private Page translatePage(fi.otavanopisto.mwp.client.model.Page managementPage) {
    Page page = new Page();
    
    PageId managementPageId = new PageId(ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));
    PageId kuntaApiPageId = idController.translatePageId(managementPageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiPageId == null) {
      logger.severe(String.format("Could not translate page %d into management page id", managementPage.getId()));
      return null;
    }
    
    PageId kuntaApiParentPageId = null;
    
    if (managementPage.getParent() != null && managementPage.getParent() > 0) {
      PageId managementParentPageId = new PageId(ManagementConsts.IDENTIFIER_NAME,String.valueOf(managementPage.getParent()));
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
}