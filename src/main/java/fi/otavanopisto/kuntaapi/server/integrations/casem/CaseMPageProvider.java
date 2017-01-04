package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    return listPages(organizationId, parentId, onlyRootPages);
  }
  
  private List<Page> listPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages) {
    List<PageId> pageIds = caseMCache.listOrganizationPageIds(organizationId);
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
      Page page = caseMCache.findPage(pageId);
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
  
  @Override
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
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
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
