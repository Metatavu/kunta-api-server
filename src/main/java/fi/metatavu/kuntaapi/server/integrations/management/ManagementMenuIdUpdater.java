package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menu;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdUpdater;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationMenusTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.metatavu.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementMenuIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private OrganizationMenusTaskQueue organizationMenusTaskQueue;
  
  @Inject
  private Event<TaskRequest> taskRequest;

  @Override
  public String getName() {
    return "management-menu-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationMenusTaskQueue.next();
    if (task != null) {
      updateManagementMenus(task.getOrganizationId());
    } else if (organizationMenusTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationMenusTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementMenus(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
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
        ApiResponse<Object> response = api.kuntaApiMenusIdHead(managementMenuId.getId());
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
