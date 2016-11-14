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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.MwpConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.mwp.client.DefaultApi;
import fi.otavanopisto.mwp.client.model.Menu;
import fi.otavanopisto.mwp.client.model.Menuitem;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementMenuEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-menus";
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
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, MwpConsts.ORGANIZATION_SETTING_BASEURL) == null) {
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
    List<Menu> managementMenus = listManagementMenus(organizationId);
    for (Menu managementMenu : managementMenus) {
      updateManagementMenu(managementMenu);
    }
    
    List<Menuitem> managementMenuItems = listManagementMenuItems(organizationId, managementMenus);
    for (Menuitem managementMenuItem : managementMenuItems) {
      updateManagementMenuItem(managementMenuItem);
    }
  }

  private List<Menu> listManagementMenus(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    fi.otavanopisto.mwp.client.ApiResponse<List<Menu>> response = api.kuntaApiMenusGet(null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s menus failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private List<Menuitem> listManagementMenuItems(OrganizationId organizationId, List<Menu> menus) {
    List<Menuitem> result = new ArrayList<>();
    
    DefaultApi api = managementApi.getApi(organizationId);
    for (Menu menu : menus) {
      String menuId = String.valueOf(menu.getId());
      fi.otavanopisto.mwp.client.ApiResponse<List<Menuitem>> response = api.kuntaApiMenusMenuIdItemsGet(menuId);
      if (response.isOk()) {
        result.addAll(response.getResponse());
      } else {
        logger.warning(String.format("Listing menu %d items failed on [%d] %s", menu.getId(), response.getStatus(), response.getMessage()));
      }
    }
    
    return result;
  }
  
  private void updateManagementMenu(Menu managementMenu) {
    MenuId menuId = new MenuId(MwpConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));

    Identifier identifier = identifierController.findIdentifierById(menuId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(menuId);
    }
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementMenu));
  }
  
  private void updateManagementMenuItem(Menuitem managementMenuItem) {
    MenuItemId menuItemId = new MenuItemId(MwpConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));

    Identifier identifier = identifierController.findIdentifierById(menuItemId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(menuItemId);
    }
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementMenuItem));
  }

}
