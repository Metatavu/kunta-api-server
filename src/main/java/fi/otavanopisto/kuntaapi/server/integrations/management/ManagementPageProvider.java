package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.debug.Timed;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementPageResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementPageContentResourceContainer;

/**
 * Page provider for management service
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementPageProvider extends AbstractManagementProvider implements PageProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ManagementPageResourceContainer managementPageResourceContainer;
  
  @Inject
  private ManagementPageContentResourceContainer managementPageContentResourceContainer;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;
  
  @Inject
  private ManagementImageLoader managementImageLoader;

  @Inject
  private IdController idController;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
   
  @Override
  @Timed (infoThreshold = 100, warningThreshold = 200, severeThreshold = 400)
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, boolean includeUnmappedParentIds) {
    return listPages(organizationId, parentId, onlyRootPages, includeUnmappedParentIds);
  }

  @Override
  @Timed (infoThreshold = 25, warningThreshold = 50, severeThreshold = 100)
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return managementPageResourceContainer.get(pageId);
  }
  
  @Override
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    List<LocalizedValue> contents = managementPageContentResourceContainer.get(pageId);
    if (contents != null) {
      return contents;
    }
    
    return Collections.emptyList();
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId, String type) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, pageId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = managementAttachmentResourceContainer.get(attachmentId);
      if (attachment != null && (type == null || StringUtils.equals(attachment.getType(), type))) {
        result.add(attachment);
      }
    }

    return result;
  }

  @Override
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    if (!identifierRelationController.isChildOf(pageId, attachmentId)) {
      return null;
    }
    
    return managementAttachmentResourceContainer.get(attachmentId);
  }

  @Override
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId,
      Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.metatavu.management.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
   
    AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
    if (size != null) {
      return scaleImage(imageData, size);
    } else {
      return imageData;
    }
  }

  private List<Page> listPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, boolean includeUnmappedParentIds) {
    PageId kuntaApiParentId = null;
    if (parentId != null) {
      kuntaApiParentId = idController.translatePageId(parentId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentId == null) {
        logger.severe(String.format("Could not translate page %s into Kunta API page id", parentId.getId()));
        return Collections.emptyList();
      }
    }
    
    List<PageId> pageIds;
    
    if (includeUnmappedParentIds) {
      return listIncludingUnmappedParentIds(organizationId, kuntaApiParentId, onlyRootPages);
    } else {
      if (onlyRootPages) {
        pageIds = identifierRelationController.listPageIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
      } else if (kuntaApiParentId != null) {
        pageIds = identifierRelationController.listPageIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, kuntaApiParentId);
      } else {
        pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
      }
    }
    
    List<Page> result = new ArrayList<>(pageIds.size());
    for (PageId pageId : pageIds) {
      Page page = managementPageResourceContainer.get(pageId);
      if (page != null) {
        result.add(page);
      }
    }
    
    return result;
  }

  private List<Page> listIncludingUnmappedParentIds(OrganizationId organizationId, PageId kuntaApiParentId, boolean onlyRootPages) {
    if (kuntaApiParentId == null && !onlyRootPages) {
      return Collections.emptyList();
    }
    
    List<Page> result = new ArrayList<>();
    
    List<PageId> pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (PageId pageId : pageIds) {
      Page page = managementPageResourceContainer.get(pageId);
      if (page == null) {
        continue;
      }
      
      if (onlyRootPages) {
        if (isAcceptableRootPageIncludingUnmapped(page)) {
          result.add(page);    
        }
      } else if (isAcceptablePageByParentIncludingUnmapped(organizationId, page, kuntaApiParentId)) {
        result.add(page);
      }
    }

    return result;
  }
  
  private boolean isAcceptablePageByParentIncludingUnmapped(OrganizationId organizationId, Page page, PageId kuntaApiParentId) {
    PageId pageParentPageId = kuntaApiIdFactory.createPageId(organizationId, page.getParentId());
    PageId unmappedPageParentPageId = kuntaApiIdFactory.createPageId(organizationId, page.getMeta().getUnmappedParentId());
    
    if (idController.idsEqual(pageParentPageId, kuntaApiParentId) || idController.idsEqual(unmappedPageParentPageId, kuntaApiParentId)) {
      return true;
    }

    return false;
  }

  private boolean isAcceptableRootPageIncludingUnmapped(Page page) {
    if (page.getParentId() == null) {
      return true;
    }
    
    if ("ROOT".equals(page.getMeta().getUnmappedParentId())) {
      return true;
    }
    
    return false;
  }

}
