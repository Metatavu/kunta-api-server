package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.PageSearcher;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;

@Dependent
public class PageController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private PageSearcher pageSearcher;

  @Inject
  private Instance<PageProvider> pageProviders;

  public List<Page> listPages(OrganizationId organizationId, String path, boolean onlyRootPages, PageId parentId, Long firstResult, Long maxResults) {
    List<Page> result = new ArrayList<>();
    
    for (PageProvider pageProvider : getPageProviders()) {
      List<Page> pages = pageProvider.listOrganizationPages(organizationId, parentId, onlyRootPages, path);
      if (pages != null) {
        result.addAll(pages);
      } else {
        logger.severe(String.format("Page provider %s returned null when listing pages", pageProvider.getClass().getName())); 
      }
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return result.subList(firstIndex, toIndex);
  }

  public List<Page> searchPages(OrganizationId organizationId, String queryString, Long firstResult, Long maxResults) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return Collections.emptyList();
    }
    
    SearchResult<PageId> searchResult = pageSearcher.searchPages(kuntaApiOrganizationId.getId(), queryString, firstResult, maxResults);
    if (searchResult != null) {
      List<Page> result = new ArrayList<>(searchResult.getResult().size());
      
      for (PageId pageId : searchResult.getResult()) {
        Page page = findPage(organizationId, pageId);
        if (page != null) {
          result.add(page);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }
  
  public Page findPage(OrganizationId organizationId, PageId pageId) {
    for (PageProvider pageProvider : getPageProviders()) {
      Page page = pageProvider.findOrganizationPage(organizationId, pageId);
      if (page != null) {
        return page;
      }
    }
    
    return null;
  }

  /**
   * Returns page contents as list of LocalizedValues. If the page is not provided by 
   * this provider, null is returned instead
   * 
   * @param organizationId organization id
   * @param pageId pageId
   * @return Returns page contents as list of LocalizedValues or null if page is not found
   */
  @SuppressWarnings ("squid:S1168")
  public List<LocalizedValue> getPageContents(OrganizationId organizationId, PageId pageId) {
    for (PageProvider pageProvider : getPageProviders()) {
      List<LocalizedValue> pageContents = pageProvider.findOrganizationPageContents(organizationId, pageId);
      if (pageContents != null) {
        return pageContents;
      }
    }
    
    return null;
  }

  public List<Attachment> listPages(OrganizationId organizationId, PageId pageId) {
    List<Attachment> result = new ArrayList<>();
   
    for (PageProvider pageProvider : getPageProviders()) {
      result.addAll(pageProvider.listOrganizationPageImages(organizationId, pageId));
    }
    
    return result;
  }
  
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    for (PageProvider pageProvider : getPageProviders()) {
      Attachment attachment = pageProvider.findPageImage(organizationId, pageId, attachmentId);
      if (attachment != null) {
        return attachment;
      }
    }
    
    return null;
  }
  
  public AttachmentData getPageAttachmentData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId, Integer size) {
    for (PageProvider pageProvider : getPageProviders()) {
      AttachmentData attachmentData = pageProvider.getPageImageData(organizationId, pageId, attachmentId, size);
      if (attachmentData != null) {
        return attachmentData;
      }
    }
    
    return null;
  }
  
  private List<PageProvider> getPageProviders() {
    List<PageProvider> result = new ArrayList<>();
    
    Iterator<PageProvider> iterator = pageProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
