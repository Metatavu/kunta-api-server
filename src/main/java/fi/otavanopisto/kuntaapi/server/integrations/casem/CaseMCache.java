package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.casem.client.ApiResponse;
import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeList;
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
  private CaseMApi caseMApi;
  
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
  
  private boolean blooBloo;
  
  @PostConstruct
  public void init() {
    blooBloo = false;
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
  
  public void refreshContents(OrganizationId organizationId) {
    if (blooBloo) {
      return;
    }
    
    blooBloo = true;
    
    Long caseMRootNodeId = getCaseMRootNodeId(organizationId);
    if (caseMRootNodeId == null) {
      logger.severe(String.format("Organization %s CaseM root node is not defined", organizationId.toString()));
    }
    
    List<Node> nodes = getChildNodes(organizationId, caseMRootNodeId, Collections.emptyList());
    cacheNodeTree(organizationId, caseMRootNodeId, nodes, new ArrayList<>()); 
  }
  
  private void cacheNodeTree(OrganizationId organizationId, Long caseMRootNodeId, List<Node> nodes, List<Long> caseMParentIds) {
    for (Node node : nodes) {
      cacheNode(organizationId, caseMRootNodeId, node);
      List<Long> childCaseMParentIds = new ArrayList<>(caseMParentIds);
      childCaseMParentIds.add(node.getNodeId());
      cacheNodeTree(organizationId, caseMRootNodeId, getChildNodes(organizationId, caseMRootNodeId, childCaseMParentIds), childCaseMParentIds);
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

  private void cacheNode(OrganizationId organizationId, Long caseMRootNodeId, Node node) {
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

  private List<Node> getChildNodes(OrganizationId organizationId, Long caseMRootNodeId, List<Long> caseMParentIds) {
    List<Node> result = new ArrayList<>();
    Long skipToken = null;
    
    do {
      NodeList nodeList = getChildNodeList(organizationId, caseMRootNodeId, caseMParentIds, skipToken);
      if (nodeList == null) {
        break;
      }
      
      result.addAll(nodeList.getValue());
      
      skipToken = getSkipToken(nodeList.getOdataNextLink());
    } while (skipToken != null);
    
    Collections.sort(result, new NodeComparator());
    
    return result;
  }
  
  private NodeList getChildNodeList(OrganizationId organizationId, Long caseMRootNodeId, List<Long> caseMParentIds, Long skipToken) {
    String pathQuery = getSubNodePath(caseMParentIds);
    
    ApiResponse<NodeList> response = caseMApi.getNodesApi(organizationId)
      .listSubNodes(caseMRootNodeId, pathQuery, skipToken != null ? String.valueOf(skipToken) : null);
    
    if (!response.isOk()) {
      logger.severe(String.format("Listing nodes by rootNode %d and pathQuery %s failed on [%d] %s", caseMRootNodeId, pathQuery, response.getStatus(), response.getMessage()));
      return null;
    } else {
      return response.getResponse();
    }
  }
  
  private String getSubNodePath(List<Long> caseMParentIds) {
    StringBuilder result = new StringBuilder();
    
    for (Long caseMParentId : caseMParentIds) {
      result.append(String.format("SubNodes(%d)/", caseMParentId));
    }
    
    result.append("SubNodes()");
    
    return result.toString();
  }
  
  private Long getSkipToken(String nextLink) {
    if (StringUtils.isBlank(nextLink)) {
      return null;
    }
    
    Pattern pattern = Pattern.compile("(.*\\$skiptoken=)([0-9]*)");
    Matcher matcher = pattern.matcher(nextLink);
    if (matcher.matches() && (matcher.groupCount() > 1)) {
      return NumberUtils.createLong(matcher.group(2));
    }
    
    return null;
  }
  
  private Long getCaseMRootNodeId(OrganizationId organizationId) {
    String rootNode = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE);
    if (StringUtils.isNumeric(rootNode)) {
      return NumberUtils.createLong(rootNode);
    }
    
   return null;
  }

  private class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node node1, Node node2) {
      Integer sortOrder1 = node1.getSortOrder();
      if (sortOrder1 == null) {
        sortOrder1 = 0;
      }
      
      Integer sortOrder2 = node1.getSortOrder();
      if (sortOrder2 == null) {
        sortOrder2 = 0;
      }

      return sortOrder1.compareTo(sortOrder2);
    }
  }
}
