package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
public class CaseMCache {

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private CaseMNodeTreeCache treeCache;

  @Inject
  private CaseMNodePageCache nodePageCache;

  @Inject
  private CaseMPageContentCache contentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  public List<Page> listRootPages(OrganizationId organizationId) {
    return listCachedPages(organizationId, null);
  }

  public List<Page> listPages(OrganizationId organizationId, PageId parentId) {
    return listCachedPages(organizationId, parentId);
  }

  public Page findPage(OrganizationId organizationId, PageId pageId) {
    return nodePageCache.get(getPageCacheKey(organizationId, pageId));
  }

  public Page findPageByPath(OrganizationId organizationId, String path) {
    Page current = null;
    
    String[] slugs = StringUtils.split(path, "/");
    for (String slug : slugs) {
      if (current == null) {
        current = findPageByParentAndSlug(organizationId, null, slug);
      } else {
        current = findPageByParentAndSlug(organizationId, new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, current.getId()), slug);
      }
      
      if (current == null) {
        return null;
      }
    }
    
    return current;
  }

  public void cacheNode(OrganizationId organizationId, Page page) {
    PageId pageId = page.getId() == null ? null : new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getId());
    PageId parentId = page.getParentId() == null ? null : new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getParentId());
    String pageCacheKey = getPageCacheKey(organizationId, pageId);
    String parentCacheKey = getPageCacheKey(organizationId, parentId);
    
    List<PageId> cacheChildIds = treeCache.get(parentCacheKey);
    if (cacheChildIds == null) {
      cacheChildIds = new ArrayList<>(1);
      cacheChildIds.add(pageId);
      treeCache.put(parentCacheKey, cacheChildIds);
    } else {
      if (!cacheChildIds.contains(pageId)) {
        cacheChildIds.add(pageId);
      }
      
      treeCache.put(parentCacheKey, cacheChildIds);
    }
    
    nodePageCache.put(pageCacheKey, page);
  }
  
  public void cachePageContents(OrganizationId organizationId, PageId pageId, String content) {
    PageId kuntaApiPageId = translatePageId(pageId);
    if (kuntaApiPageId == null) {
      logger.severe(String.format("PageId %s could not be translated into kunta api id", pageId.toString()));
      return;
    }
    
    contentCache.put(getPageCacheKey(organizationId, kuntaApiPageId), content);
    modificationHashCache.put(kuntaApiPageId.getId(), DigestUtils.md5Hex(content));
  }

  public void removePage(PageId pageId) {
    String cacheKey = getPageCacheKey(pageId.getOrganizationId(), pageId);
    treeCache.clear(cacheKey);
    contentCache.clear(cacheKey);
    nodePageCache.clear(cacheKey);
  }

  public List<LocalizedValue> getPageContent(OrganizationId organizationId, PageId pageId) {
    String content = contentCache.get(getPageCacheKey(organizationId, pageId));
    if (StringUtils.isBlank(content)) {
      return Collections.emptyList();
    }
    
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
    localizedValue.setValue(content);
    
    return Collections.singletonList(localizedValue);
  }
  
  public List<PageId> listOrganizationPageIds(OrganizationId organizationId) {
    return listAllPageIds(organizationId, null);
  }
  
  private Page findPageByParentAndSlug(OrganizationId organizationId, PageId parentId, String slug) {
    List<Page> pages = listCachedPages(organizationId, parentId);
    for (Page page : pages) {
      if (page.getSlug().equals(slug)) {
        return page;
      }
    }
    
    return null;
  }

  private List<Page> listCachedPages(OrganizationId organizationId, PageId parentId) {
    List<PageId> pageIds = listPageIds(organizationId, parentId);
    
    List<Page> pages = new ArrayList<>(pageIds.size());
    
    for (PageId pageId : pageIds) {
      Page page = findPage(organizationId, pageId);
      if (page != null) {
        pages.add(page);
      }
    }
    
    return pages;
  }
  
  private List<PageId> listAllPageIds(OrganizationId organizationId, PageId parentId) {
    List<PageId> result = new ArrayList<>();
    
    List<PageId> pageIds = listPageIds(organizationId, parentId);
    for (PageId pageId : pageIds) {
      result.addAll(listAllPageIds(organizationId, pageId));
    }
    
    result.addAll(pageIds);
    
    return result;
  }

  private List<PageId> listPageIds(OrganizationId organizationId, PageId parentId) {
    List<PageId> pageIds = treeCache.get(getPageCacheKey(organizationId, parentId));
    if (pageIds == null) {
      return Collections.emptyList();
    }
    
    return pageIds;
  }
  
  private String getPageCacheKey(OrganizationId organizationId, PageId pageId) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId != null) {      
      if (pageId == null) {
        return String.format("%s-root", kuntaApiOrganizationId.getId());
      } else {
        PageId kuntaApiPageId = translatePageId(pageId);
        if (kuntaApiPageId == null) {
          logger.severe(String.format("PageId %s could not be translated into kunta api id", pageId.toString()));
          return null;
        }
        
        return String.format("%s-%s", kuntaApiOrganizationId.getId(), kuntaApiPageId.getId());
      }
    } else {  
      logger.severe(String.format("Organization %s could not be translated into kunta api id", organizationId.toString()));
      return null;
    }
  }
  
  private PageId translatePageId(PageId pageId) {
    PageId result = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (result != null) {
      return result;
    }
    
    return result;
  }

}
