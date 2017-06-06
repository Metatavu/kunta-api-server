package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

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
import fi.otavanopisto.kuntaapi.server.index.IndexRemovePage;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;

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
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;
  
  @Override
  @Timed (infoThreshold = 100, warningThreshold = 200, severeThreshold = 400)
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, boolean includeUnmappedParentIds) {
    return listPages(organizationId, parentId, onlyRootPages, includeUnmappedParentIds);
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
        pageIds = identifierRelationController.listPageIdsBySourceAndParentId(CaseMConsts.IDENTIFIER_NAME, organizationId);
      } else if (kuntaApiParentId != null) {
        pageIds = identifierRelationController.listPageIdsBySourceAndParentId(CaseMConsts.IDENTIFIER_NAME, kuntaApiParentId);
      } else {
        pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, CaseMConsts.IDENTIFIER_NAME);
      }
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

  @Override
  public void deleteOrganizationPage(OrganizationId organizationId, PageId pageId) {
    PageId casemPageId = idController.translatePageId(pageId, CaseMConsts.IDENTIFIER_NAME);
    if (casemPageId != null) {
      Page page = caseMCache.findPage(pageId);
      if (page != null) {
        caseMCache.removePage(pageId);
        IndexRemovePage indexRemovePage = new IndexRemovePage();
        indexRemovePage.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
        indexRemovePage.setPageId(page.getId());
        indexRemoveRequest.fire(new IndexRemoveRequest(indexRemovePage));
      }
    }
  }
  
  private List<Page> listIncludingUnmappedParentIds(OrganizationId organizationId, PageId kuntaApiParentId, boolean onlyRootPages) {
    if (kuntaApiParentId == null && !onlyRootPages) {
      return Collections.emptyList();
    }
    
    List<Page> result = new ArrayList<>();
    
    List<PageId> pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, CaseMConsts.IDENTIFIER_NAME);
    for (PageId pageId : pageIds) {
      Page page = caseMCache.findPage(pageId);
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
