package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menu;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.discover.MenuIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementMenuIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 5000;
  
  @Inject
  private Logger logger;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private Event<MenuIdUpdateRequest> idUpdateRequest;

  private boolean stopped;
  private List<OrganizationId> queue;
  
  @Resource
  private TimerService timerService;
  
  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-menu-ids";
  }
  
  @Override
  public void startTimer() {
    stopped = false;
    startTimer(WARMUP_TIME);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Asynchronous
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
      if (!queue.isEmpty()) {
        updateManagementMenus(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementMenus(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<Menu> managementMenus = listManagementMenus(api, organizationId);
    for (int i = 0, l = managementMenus.size(); i < l; i++) {
      Menu managementMenu = managementMenus.get(i);
      MenuId menuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));
      idUpdateRequest.fire(new MenuIdUpdateRequest(organizationId, menuId, (long) i, false));
    }
    
    
  }

  private List<Menu> listManagementMenus(DefaultApi api, OrganizationId organizationId) {
    fi.metatavu.management.client.ApiResponse<List<Menu>> response = api.kuntaApiMenusGet(null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s menus failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
}
