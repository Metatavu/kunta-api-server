package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;

/**
 * Page provider for CaseM
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class CaseMPageProvider implements PageProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private CaseMCache caseMCache;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  @Timed (infoThreshold = 100, warningThreshold = 200, severeThreshold = 400)
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    return listPages(organizationId, parentId, onlyRootPages);
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
      pageIds = identifierRelationController.listPageIdsBySourceAndParentId(CaseMConsts.IDENTIFIER_NAME, organizationId);
    } else if (kuntaApiParentId != null) {
      pageIds = identifierRelationController.listPageIdsBySourceAndParentId(CaseMConsts.IDENTIFIER_NAME, kuntaApiParentId);
    } else {
      pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, CaseMConsts.IDENTIFIER_NAME);
    }
    
    List<Page> result = new ArrayList<>(pageIds.size());
    for (PageId pageId : pageIds) {
      Page page = caseMCache.findPage(pageId);
      if (page != null) {
        result.add(page);
      }
    }
      
    return result;
  }
  
  @Override
  @Timed (infoThreshold = 25, warningThreshold = 50, severeThreshold = 100)
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return caseMCache.findPage(pageId);
  }
  
  @Override
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    List<LocalizedValue> content = caseMCache.getPageContent(pageId);
    if (content != null) {
      return content;
    }
    
    return Collections.emptyList();
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId, String type) {
    return Collections.emptyList();
  }

  @Override
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    return null;
  }

  @Override
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId, Integer size) {
    return null;
  }
  
}
