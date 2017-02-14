package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Pagemappings;
import fi.otavanopisto.kuntaapi.server.controllers.PageController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.IdMapProvider.OrganizationPageMap;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementPageIdMapCache;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageIdMapEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 60;

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
  private ManagementPageIdMapCache managementPageIdMapCache;
  
  @Resource
  private TimerService timerService;
  
  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "management-page-id-map";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }      
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning() && !queue.isEmpty()) {
        updatePageIdMap(queue.remove(0));          
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updatePageIdMap(OrganizationId organizationId) {
    OrganizationPageMap pageIdMap = loadPageIdMap(organizationId);
    if (pageIdMap != null) {
      managementPageIdMapCache.put(organizationId, pageIdMap);
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
    List<Page> childPages = pageController.listPages(parentId.getOrganizationId(), null, false, parentId, null, null);
    
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
    
    Page page = pageController.findPageByPath(organizationId, path);
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
