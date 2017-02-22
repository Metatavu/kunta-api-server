package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menu;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationMenusTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementMenuIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 1000 * 60 * 10;
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private OrganizationMenusTaskQueue organizationMenusTaskQueue;
  
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-menu-ids";
  }
  
  @Override
  public void startTimer() {
    startTimer(WARMUP_TIME);
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationMenusTaskQueue.next();
      if (task != null) {
        updateManagementMenus(task.getOrganizationId());
      } else {
        organizationMenusTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
      }
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateManagementMenus(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    checkRemovedManagementMenus(api, organizationId);
    
    List<Menu> managementMenus = listManagementMenus(api, organizationId);
    for (int i = 0, l = managementMenus.size(); i < l; i++) {
      Menu managementMenu = managementMenus.get(i);
      MenuId menuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<MenuId>(Operation.UPDATE, menuId, (long) i)));
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
    
  private void checkRemovedManagementMenus(DefaultApi api, OrganizationId organizationId) {
    List<MenuId> menuIds = identifierController.listOrganizationMenuIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (MenuId menuId : menuIds) {
      MenuId managementMenuId = idController.translateMenuId(menuId, ManagementConsts.IDENTIFIER_NAME);
      if (managementMenuId != null) {
        ApiResponse<Menu> response = api.kuntaApiMenusIdGet(managementMenuId.getId());
        int status = response.getStatus();
        // If status is 404 the menu has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the menu should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<MenuId>(Operation.REMOVE, menuId)));
        }
      }
    }
  }
  
}
