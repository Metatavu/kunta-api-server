package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeName;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
public class CaseMCache {

  private static final int MAX_SLUG_LENGTH = 30;

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private CaseMNodeTreeCache treeCache;

  @Inject
  private CaseMNodePageCache pageCache;
  
  private Map<PageId, PageId> discoveredPageIds;
  
  @PostConstruct
  public void init() {
    discoveredPageIds = new HashMap<>();
  }

  public List<Page> listRootPages(OrganizationId organizationId) {
    return listCachedPages(organizationId, null);
  }

  public List<Page> listPages(OrganizationId organizationId, PageId parentId) {
    return listCachedPages(organizationId, parentId);
  }

  public Page findPage(OrganizationId organizationId, PageId pageId) {
    return pageCache.get(getPageCacheKey(organizationId, pageId));
  }

  public Page findPageByPath(OrganizationId organizationId, String path) {
    Page current = null;
    
    String[] slugs = StringUtils.split(path, "/");
    for (String slug : slugs) {
      if (current == null) {
        current = findPageByParentAndSlug(organizationId, null, slug);
      } else {
        current = findPageByParentAndSlug(organizationId, new PageId(KuntaApiConsts.IDENTIFIER_NAME, current.getId()), slug);
      }
      
      if (current == null) {
        return null;
      }
    }
    
    return current;
  }

  public void cacheNode(OrganizationId organizationId, Long caseMRootNodeId, Node node) {
    PageId pageId = caseMRootNodeId.equals(node.getNodeId()) ? null : translatePageId(toNodeId(node.getNodeId()), true);
    PageId parentId = caseMRootNodeId.equals(node.getParentId()) ? null : translatePageId(toNodeId(node.getParentId()), true);
    String pageCacheKey = getPageCacheKey(organizationId, pageId);
    String parentCacheKey = getPageCacheKey(organizationId, parentId);
    
    List<PageId> cacheChildIds = treeCache.get(parentCacheKey);
    if (cacheChildIds == null) {
      cacheChildIds = new ArrayList<>(1);
      cacheChildIds.add(pageId);
      treeCache.put(parentCacheKey, cacheChildIds);
    } else {
      cacheChildIds.add(pageId);
      treeCache.put(parentCacheKey, cacheChildIds);
    }
    
    pageCache.put(pageCacheKey, translateNode(organizationId, caseMRootNodeId, node));
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
    List<PageId> pageIds = treeCache.get(getPageCacheKey(organizationId, parentId));
    if (pageIds == null) {
      return Collections.emptyList();
    }
    
    List<Page> pages = new ArrayList<>(pageIds.size());
    
    for (PageId pageId : pageIds) {
      Page page = findPage(organizationId, pageId);
      if (page != null) {
        pages.add(page);
      }
    }
    
    return pages;
  }
  
  private Page translateNode(OrganizationId organizationId, Long caseMRootNodeId, Node node) {
    Page page = new Page();

    PageId kuntaApiId = translatePageId(new PageId(CaseMConsts.IDENTIFIER_NAME, String.valueOf(node.getNodeId())), false);
    PageId kuntaApiParentId = caseMRootNodeId.equals(node.getParentId()) ? null : translatePageId(new PageId(CaseMConsts.IDENTIFIER_NAME, String.valueOf(node.getParentId())), false);
    List<LocalizedValue> titles = translateNodeNames(organizationId, node.getNames());
    page.setId(kuntaApiId.getId());
    page.setParentId(kuntaApiParentId != null ? kuntaApiParentId.getId() : null);
    page.setTitles(titles);
    page.setSlug(slugify(titles.isEmpty() ? kuntaApiId.getId() : titles.get(0).getValue()));
    
    return page;
  }
  
  private String slugify(String title) {
    return slugify(title, MAX_SLUG_LENGTH );
  }
  
  private String slugify(String text, int maxLength) {
    String urlName = StringUtils.normalizeSpace(text);
    if (StringUtils.isBlank(urlName))
      return UUID.randomUUID().toString();
    
    urlName = StringUtils.lowerCase(StringUtils.substring(StringUtils.stripAccents(urlName.replaceAll(" ", "_")).replaceAll("[^a-zA-Z0-9\\-\\.\\_]", ""), 0, maxLength));
    if (StringUtils.isBlank(urlName)) {
      urlName = UUID.randomUUID().toString();
    }
    
    return urlName;
  }

  private List<LocalizedValue> translateNodeNames(OrganizationId organizationId, List<NodeName> names) {
    List<LocalizedValue> result = new ArrayList<>(names.size());
    
    for (NodeName name : names) {
      LocalizedValue localizedValue = new LocalizedValue();
      
      String language = translateLanguage(organizationId, name.getLanguageId());
      localizedValue.setLanguage(language);
      localizedValue.setValue(name.getName());
      result.add(localizedValue);
    }
    
    return result;
  }
  
  private String translateLanguage(OrganizationId organizationId, Long localeId) {
    OrganizationSetting localeSetting = organizationSettingController.findOrganizationSettingByKey(organizationId, String.format(CaseMConsts.ORGANIZATION_SETTING_LOCALE_ID, localeId));
    if (localeSetting != null) {
      return localeSetting.getValue();
    }
    
    return CaseMConsts.DEFAULT_LANGUAGE;
  }
  
  private String getPageCacheKey(OrganizationId organizationId, PageId pageId) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId != null) {      
      if (pageId == null) {
        return String.format("%s-root", kuntaApiOrganizationId.getId());
      } else {
        PageId kuntaApiPageId = translatePageId(pageId, false);
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
  
  private PageId translatePageId(PageId pageId, boolean createMissing) {
    PageId result = discoveredPageIds.get(pageId);
    if (result != null) {
      return result;
    }
    
    result = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (result != null) {
      return result;
    }
    
    if (createMissing) {
      Identifier identifier = identifierController.createIdentifier(pageId);
      result = new PageId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      discoveredPageIds.put(pageId, result);
    }
    
    return result;
  }
  
  private PageId toNodeId(Long nodeId) {
    return new PageId(CaseMConsts.IDENTIFIER_NAME, String.valueOf(nodeId));
  }

}
