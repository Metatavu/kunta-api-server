package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Pagemappings;
import fi.metatavu.kuntaapi.server.controllers.PageController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.integrations.IdMapProvider.OrganizationPageMap;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementPageIdMapResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationPageMapsTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageIdMapEntityDiscoverJob extends EntityDiscoverJob<OrganizationEntityUpdateTask> {

  @Inject
  private Logger logger;
  
  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private PageController pageController;

  @Inject
  private ManagementPageIdMapResourceContainer managementPageIdMapResourceContainer;
  
  @Inject
  private OrganizationPageMapsTaskQueue organizationPageMapsTaskQueue;

  @Override
  public String getName() {
    return "management-page-id-map";
  }
  
  @Override
  public void execute(OrganizationEntityUpdateTask task) {
    updatePageIdMap(task.getOrganizationId());
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationPageMapsTaskQueue.next();
      if (task != null) {
        execute(task);
      } else if (organizationPageMapsTaskQueue.isEmptyAndLocalNodeResponsible()) {
        organizationPageMapsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
      }
    }
  }
  
  private void updatePageIdMap(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    OrganizationPageMap pageIdMap = loadPageIdMap(organizationId);
    if (pageIdMap != null) {
      managementPageIdMapResourceContainer.put(organizationId, pageIdMap);
    }
  }
  
  private OrganizationPageMap loadPageIdMap(OrganizationId organizationId) {
    Map<String, String> pathMap = loadPagePathMap(organizationId);
    if (pathMap == null) {
      return null;
    }
    
    OrganizationPageMap result = new OrganizationPageMap();
    
    for (Map.Entry<String, String> pathEntry : pathMap.entrySet()) {
      String pagePath = pathEntry.getKey();
      boolean wildcard = StringUtils.endsWith(pagePath, "*");
          
      if (wildcard) {
        pagePath = StringUtils.removeEnd(pagePath, "*");
      }
      
      pagePath = StringUtils.removeEnd(pagePath, "/");
      
      BaseId parentId = resolvePageIdByPath(organizationId, pathEntry.getValue());
      if (parentId == null) {
        continue;
      }
    
      BaseId pageId = resolvePageIdByPath(organizationId, pagePath);
      if (pageId instanceof PageId) {
        applyMappings(result, wildcard, parentId, (PageId) pageId);
      }
    }
    
    return result;
  }

  private void applyMappings(Map<BaseId, BaseId> result, boolean wildcard, BaseId parentId, PageId pageId) {
    if (wildcard) {
      for (PageId childPageId : listChildPageIds((PageId) pageId)) {
        result.put(childPageId, parentId);
      }
    } else {
      result.put(pageId, parentId);
    }
  }
  
  private List<PageId> listChildPageIds(PageId parentId) {
    List<Page> childPages = pageController.listPages(parentId.getOrganizationId(), null, false, parentId, true, null, null);
    
    List<PageId> result = new ArrayList<>(childPages.size());
    for (Page childPage : childPages) {
      result.add(new PageId(parentId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME, childPage.getId()));
    }
    
    return result;
  }
  
  private BaseId resolvePageIdByPath(OrganizationId organizationId, String path) {
    if (StringUtils.isBlank(path)) {
      return organizationId;
    }
    
    Page page = pageController.findPageByPath(organizationId, path, true);
    if (page != null) {
      return new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getId());
    }
    
    return null;
  }
  
  private Map<String, String> loadPagePathMap(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    ApiResponse<List<Pagemappings>> response = api.kuntaApiPagemappingsGet();
    if (response.isOk()) {
      List<Pagemappings> mappings = response.getResponse();
      if (mappings == null) {
        return null;
      }
      
      Map<String, String> result = new HashMap<>(mappings.size());
      for (Pagemappings mappping : mappings) {
        result.put(mappping.getPagePath(), mappping.getParentPath());
      }
      
      return result;
    } else {
      logger.log(Level.SEVERE, () -> String.format("Loading organization %s page id map failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
      return null;
    }
  }

}
