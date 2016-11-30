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

import fi.otavanopisto.kuntaapi.server.cache.MenuCache;
import fi.otavanopisto.kuntaapi.server.cache.MenuItemCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.MenuIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider.MenuItemType;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.DefaultApi;
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
  
  @Inject
  private IdController idController;
  
  @Inject
  private MenuCache menuCache;

  @Inject
  private MenuItemCache menuItemCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<MenuIdUpdateRequest> queue;

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
  public void onMenuIdUpdateRequest(@Observes MenuIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getOrganizationId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event);
        queue.add(0, event);
      } else {
        if (!queue.contains(event)) {
          queue.add(event);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        MenuIdUpdateRequest updateRequest = queue.remove(0);
        DefaultApi api = managementApi.getApi(updateRequest.getOrganizationId());
        updateManagementMenu(api, updateRequest.getOrganizationId(), updateRequest.getId());
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementMenu(DefaultApi api, OrganizationId organizationId, MenuId managementMenuId) {
    ApiResponse<fi.otavanopisto.mwp.client.model.Menu> response = api.kuntaApiMenusIdGet(managementMenuId.getId());
    if (response.isOk()) {
      updateManagementMenu(api, organizationId, response.getResponse());
    } else {
      logger.warning(String.format("Finding organization %s menu failed on [%d] %s", managementMenuId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateManagementMenu(DefaultApi api, OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Menu managementMenu) {
    Menu menu = updateManagementMenu(organizationId, managementMenu);
    MenuId menuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, menu.getId());
    
    List<Menuitem> managementMenuItems = listManagementMenuItems(api, managementMenu);
    for (Menuitem managementMenuItem : managementMenuItems) {
      updateManagementMenuItem(organizationId, menuId, managementMenuItem);
    }
  }

  private List<Menuitem> listManagementMenuItems(DefaultApi api, fi.otavanopisto.mwp.client.model.Menu menu) {
    List<Menuitem> result = new ArrayList<>();
    String menuId = String.valueOf(menu.getId());
    fi.otavanopisto.mwp.client.ApiResponse<List<Menuitem>> response = api.kuntaApiMenusMenuIdItemsGet(menuId);
    if (response.isOk()) {
      result.addAll(response.getResponse());
    } else {
      logger.warning(String.format("Listing menu %d items failed on [%d] %s", menu.getId(), response.getStatus(), response.getMessage()));
    }
    
    return result;
  }
  
  private Menu updateManagementMenu(OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Menu managementMenu) {
    MenuId managementMenuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));

    Identifier identifier = identifierController.findIdentifierById(managementMenuId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(managementMenuId);
    }
    
    MenuId kuntaApiMenuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Menu menu = translateMenu(organizationId, managementMenu);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menu));
    menuCache.put(kuntaApiMenuId, menu);
    
    return menu;
  }
  
  private void updateManagementMenuItem(OrganizationId organizationId, MenuId menuId, Menuitem managementMenuItem) {
    MenuItemId managementMenuItemId = new MenuItemId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));

    Identifier identifier = identifierController.findIdentifierById(managementMenuItemId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(managementMenuItemId);
    }
    
    MenuItem menuItem = translateMenuItem(organizationId, managementMenuItem);
    MenuItemId kuntaApiMenuItemId = new MenuItemId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
        
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menuItem));
    menuItemCache.put(new IdPair<MenuId, MenuItemId>(menuId,kuntaApiMenuItemId), menuItem);
  }

  private Menu translateMenu(OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Menu managementMenu) {
    Menu menu = new Menu();
    
    MenuId managementMenuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));
    MenuId kuntaApiMenuId = idController.translateMenuId(managementMenuId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiMenuId == null) {
      logger.info(String.format("Could not translate management menu %d into kunta api id", managementMenu.getId()));
      return null;
    }
    
    menu.setId(kuntaApiMenuId.getId());
    menu.setSlug(managementMenu.getSlug());
    
    return menu;
  }
  
  private MenuItem translateMenuItem(OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem) {
    MenuItem menuItem = new MenuItem();
    
    MenuItemId managementMenuItemId = new MenuItemId(organizationId,ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));
    MenuItemId kuntaApiMenuItemId = idController.translateMenuItemId(managementMenuItemId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiMenuItemId == null) {
      logger.info(String.format("Could not translate management menu item %d into kunta api id", managementMenuItem.getId()));
      return null;
    }
    
    MenuItemType itemType = getItemType(managementMenuItem);
    if (itemType == null) {
      logger.severe(String.format("Could not determine item type for %d", managementMenuItem.getId()));
      return null;
    }
    
    PageId pageId = translatePageId(organizationId, managementMenuItem.getPageId());
    MenuItemId parentMenuItemId = translateMenuItemId(organizationId, managementMenuItem.getParentItemId());
    
    menuItem.setId(kuntaApiMenuItemId.getId());
    menuItem.setLabel(managementMenuItem.getTitle());
    menuItem.setFileId(null);
    menuItem.setExternalUrl(itemType == MenuItemType.LINK ? managementMenuItem.getUrl() : null);
    menuItem.setPageId(pageId != null ? pageId.getId() : null);
    menuItem.setParentItemId(parentMenuItemId != null ? parentMenuItemId.getId() : null);
    menuItem.setType(itemType.toString());
    
    return menuItem;
  }

  private MenuItemId translateMenuItemId(OrganizationId organizationId, Long parentItemId) {
    if (parentItemId == null) {
      return null;
    }
    
    MenuItemId managementMenuItem = new MenuItemId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(parentItemId));

    return idController.translateMenuItemId(managementMenuItem, KuntaApiConsts.IDENTIFIER_NAME);
  }

  private MenuItemType getItemType(fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem) {
    switch (managementMenuItem.getType()) {
      case "page":
        return MenuItemType.PAGE;
      case "post":
        return MenuItemType.NEWS_ARTICLE;
      case "custom":
        return MenuItemType.LINK;
      default:
        return null;
    }
  }
  
  private PageId translatePageId(OrganizationId organizationId, Long pageId) {
    if (pageId == null) {
      return null;
    }
    
    return translatePageId(organizationId, pageId.intValue());
  }
  
  private PageId translatePageId(OrganizationId organizationId, Integer pageId) {
    if (pageId == null) {
      return null;
    }
    
    PageId managementId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(pageId));
    return idController.translatePageId(managementId, KuntaApiConsts.IDENTIFIER_NAME);
  }
  
}
