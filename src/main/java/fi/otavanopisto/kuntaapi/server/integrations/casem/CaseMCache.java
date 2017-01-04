package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.casem.cache.CaseMPageCache;
import fi.otavanopisto.kuntaapi.server.integrations.casem.cache.CaseMPageContentCache;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMCache {

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private CaseMPageCache pageCache;
  
  @Inject
  private CaseMPageContentCache pageContentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  public Page findPage(PageId pageId) {
    return pageCache.get(pageId);
  }

  public void cacheNode(OrganizationId organizationId, Page page) {
    if (page != null) {
      PageId pageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getId());
      pageCache.put(pageId, page);
    } else {
      logger.severe(String.format("Tried to cache null CaseM page in oranization %s", organizationId));
    }
  }
  
  public void cachePageContents(PageId pageId, String content) {
    PageId kuntaApiPageId = translatePageId(pageId);
    if (kuntaApiPageId == null) {
      logger.severe(String.format("PageId %s could not be translated into kunta api id", pageId.toString()));
      return;
    }
    
    pageContentCache.put(pageId, translateLocalized(content));
    modificationHashCache.put(kuntaApiPageId.getId(), DigestUtils.md5Hex(content));
  }

  public void removePage(PageId pageId) {
    pageContentCache.clear(pageId);
    pageCache.clear(pageId);
  }

  public List<LocalizedValue> getPageContent(PageId pageId) {
    return pageContentCache.get(pageId);
  }
  
  public List<PageId> listOrganizationPageIds(OrganizationId organizationId) {
    return pageCache.getOragnizationIds(organizationId);
  }

  private PageId translatePageId(PageId pageId) {
    PageId result = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (result != null) {
      return result;
    }
    
    return result;
  }
  
  private List<LocalizedValue> translateLocalized(String content) {
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
    localizedValue.setValue(content);
    
    return Collections.singletonList(localizedValue);
  }

}
