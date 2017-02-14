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
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementAttachmentCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageContentCache;

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
  private ManagementPageCache pageCache;
  
  @Inject
  private ManagementPageContentCache pageContentCache;
  
  @Inject
  private ManagementAttachmentCache managementAttachmentCache;
  
  @Inject
  private ManagementImageLoader managementImageLoader;

  @Inject
  private IdController idController;
  
  @Override
  @Timed (infoThreshold = 100, warningThreshold = 200, severeThreshold = 400)
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    return listPages(organizationId, parentId, onlyRootPages);
  }

  @Override
  @Timed (infoThreshold = 25, warningThreshold = 50, severeThreshold = 100)
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return pageCache.get(pageId);
  }
  
  @Override
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    List<LocalizedValue> contents = pageContentCache.get(pageId);
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
      Attachment attachment = managementAttachmentCache.get(attachmentId);
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
    
    return managementAttachmentCache.get(attachmentId);
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

  private List<Page> listPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    PageId kuntaApiParentId = null;
    if (parentId != null) {
      kuntaApiParentId = idController.translatePageId(parentId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentId == null) {
        logger.severe(String.format("Could not translate page %s into Kunta API page id", parentId.getId()));
        return Collections.emptyList();
      }
    }
    
    List<PageId> pageIds;
    
    if (onlyRootPages) {
      pageIds = identifierRelationController.listPageIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    } else if (kuntaApiParentId != null) {
      pageIds = identifierRelationController.listPageIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, kuntaApiParentId);
    } else {
      pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    }
    
    List<Page> result = new ArrayList<>(pageIds.size());
    for (PageId pageId : pageIds) {
      Page page = pageCache.get(pageId);
      if (page != null) {
        result.add(page);
      }
    }
    
    return result;
  }

}
