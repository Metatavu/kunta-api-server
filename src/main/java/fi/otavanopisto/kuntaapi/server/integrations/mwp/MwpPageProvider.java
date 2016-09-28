package fi.otavanopisto.kuntaapi.server.integrations.mwp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.IdController;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;

/**
 * Page provider for management service
 * 
 * @author Antti Leppä
 */
@Dependent
public class MwpPageProvider extends AbstractMwpProvider implements PageProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private MwpApi mwpApi;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  private MwpPageProvider() {
  }
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, String path) {
    if (StringUtils.isNotBlank(path)) {
      fi.otavanopisto.mwp.client.model.Page mwpPage = findPageByPath(organizationId, path);
      if (mwpPage == null) {
        return Collections.emptyList();
      }
      
      if (parentId != null && !idController.idsEqual(parentId, translatePageId(mwpPage.getParent()))) {
        return Collections.emptyList();
      }
    
      return Collections.singletonList(translatePage(mwpPage));
    } else {
      return listPages(organizationId, parentId);
    }
  }

  @Override
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page mwpPage = findPageByPageId(organizationId, pageId);
    if (mwpPage != null) {
      return translatePage(mwpPage);
    }
  
    return null;
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
    fi.otavanopisto.mwp.client.model.Page mwpPage = findPageByPageId(organizationId, pageId);
    if (mwpPage != null) {
      Integer featuredMediaId = mwpPage.getFeaturedMedia();
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
        AttachmentId mwpAttachmentId = getImageAttachmentId(featuredMediaId);
        if (!idController.idsEqual(attachmentId, mwpAttachmentId)) {
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
      AttachmentData imageData = getImageData(featuredMedia.getSourceUrl());
      
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
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Page>> response = mwpApi.getApi(organizationId).wpV2PagesGet(
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
    PageId mwpPageId = idController.translatePageId(pageId, MwpConsts.IDENTIFIER_NAME);
    if (mwpPageId == null) {
      logger.severe(String.format("Failed to convert %s into MWP id", pageId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Page> response = mwpApi.getApi(organizationId).wpV2PagesIdGet(mwpPageId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding page failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

  private List<Page> listPages(OrganizationId organizationId, PageId parentId) {
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
    
    if (parentId != null) {
      PageId mwpParentId = idController.translatePageId(parentId, MwpConsts.IDENTIFIER_NAME);
      if (mwpParentId == null) {
        logger.severe(String.format("Could not translate %s into mwp service id", parentId.toString()));
        return Collections.emptyList();
      }
      
      parent = Collections.singletonList(mwpParentId.getId()); 
    }
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Page>> response = mwpApi.getApi(organizationId).wpV2PagesGet(
        context, page, perPage, search, after, author, authorExclude, before, authorExclude, include, menuOrder, offset,
        order, orderby, parent, parentExclude, slug, status, filter);
    if (!response.isOk()) {
      logger.severe(String.format("Page listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translatePages(response.getResponse());
    }
    
    return Collections.emptyList();
  }

  private List<Page> translatePages(List<fi.otavanopisto.mwp.client.model.Page> mwpPages) {
    List<Page> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Page mwpPage : mwpPages) {
      result.add(translatePage(mwpPage));
    }
    
    return result;
  }

  private Page translatePage(fi.otavanopisto.mwp.client.model.Page mwpPage) {
    Page page = new Page();
    
    PageId mwpId = new PageId(MwpConsts.IDENTIFIER_NAME, String.valueOf(mwpPage.getId()));
    PageId kuntaApiId = idController.translatePageId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new page %s", mwpPage.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(mwpId);
      kuntaApiId = new PageId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    PageId kuntaApiParentId = null;
    
    if (mwpPage.getParent() != null) {
      PageId mwpParentId = translatePageId(mwpPage.getParent());
      kuntaApiParentId = idController.translatePageId(mwpParentId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentId == null) {
        logger.info(String.format("Found new page %s", mwpParentId.getId()));
        Identifier newIdentifier = identifierController.createIdentifier(mwpParentId);
        kuntaApiParentId = new PageId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
      }
    }
    
    page.setContents(translateLocalized(mwpPage.getContent().getRendered()));
    page.setTitles(translateLocalized(mwpPage.getTitle().getRendered()));
    
    page.setId(kuntaApiId.getId());
    
    if (kuntaApiParentId != null) {
      page.setParentId(kuntaApiParentId.getId());
    }
    
    page.setSlug(mwpPage.getSlug());
    
    return page;
  }
}
