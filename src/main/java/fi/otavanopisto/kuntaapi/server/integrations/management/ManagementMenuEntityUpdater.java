package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
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

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menuitem;
import fi.otavanopisto.kuntaapi.server.cache.MenuCache;
import fi.otavanopisto.kuntaapi.server.cache.MenuItemCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.MenuIdRemoveRequest;
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
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementMenuEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

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
  private IdUpdateRequestQueue<MenuIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(ManagementConsts.IDENTIFIER_NAME);
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
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onMenuIdRemoveRequest(@Observes MenuIdRemoveRequest event) {
    if (!stopped) {
      MenuId menuId = event.getId();
      
      if (!StringUtils.equals(menuId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteMenu(event.getOrganizationId(), menuId);
    }
  }


  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        MenuIdUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateManagementMenu(updateRequest);
        }
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementMenu(MenuIdUpdateRequest updateRequest) {
    OrganizationId organizationId = updateRequest.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    MenuId managementMenuId = updateRequest.getId();
    Long orderIndex = updateRequest.getOrderIndex();
    
    ApiResponse<fi.metatavu.management.client.model.Menu> response = api.kuntaApiMenusIdGet(managementMenuId.getId());
    if (response.isOk()) {
      updateManagementMenu(api, organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Finding organization %s menu failed on [%d] %s", managementMenuId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateManagementMenu(DefaultApi api, OrganizationId organizationId, fi.metatavu.management.client.model.Menu managementMenu, Long orderIndex) {
    Menu menu = updateManagementMenu(organizationId, managementMenu, orderIndex);
    if (menu == null) {
      logger.warning(String.format("Failed to update menu %d on organization %s", managementMenu.getId(), organizationId.getId()));
      return;
    }
    
    MenuId menuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, menu.getId());
    List<MenuItemId> existingKuntaApiMenuItemIds = menuItemCache.getBareChildIds(menuId);
    List<Menuitem> managementMenuItems = listManagementMenuItems(api, managementMenu);
    for (int i = 0, l = managementMenuItems.size(); i < l; i++) {
      Menuitem managementMenuItem = managementMenuItems.get(i);
      MenuItemId menuItemId = updateManagementMenuItem(organizationId, menuId, managementMenuItem, (long) i);
      existingKuntaApiMenuItemIds.remove(menuItemId);
    }
    
    for (MenuItemId existingKuntaApiMenuItemId : existingKuntaApiMenuItemIds) {
      deleteMenuItem(menuId, existingKuntaApiMenuItemId);
    }
  }

  private List<Menuitem> listManagementMenuItems(DefaultApi api, fi.metatavu.management.client.model.Menu menu) {
    List<Menuitem> result = new ArrayList<>();
    String menuId = String.valueOf(menu.getId());
    fi.metatavu.management.client.ApiResponse<List<Menuitem>> response = api.kuntaApiMenusMenuIdItemsGet(menuId);
    if (response.isOk()) {
      result.addAll(response.getResponse());
    } else {
      logger.warning(String.format("Listing menu %d items failed on [%d] %s", menu.getId(), response.getStatus(), response.getMessage()));
    }
    
    return result;
  }
  
  private Menu updateManagementMenu(OrganizationId organizationId, fi.metatavu.management.client.model.Menu managementMenu, Long orderIndex) {
    MenuId managementMenuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));

    Identifier identifier = identifierController.findIdentifierById(managementMenuId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(organizationId, orderIndex, managementMenuId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, organizationId, orderIndex);
    }
    
    MenuId kuntaApiMenuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Menu menu = translateMenu(organizationId, managementMenu);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menu));
    menuCache.put(kuntaApiMenuId, menu);
    
    return menu;
  }
  
  private MenuItemId updateManagementMenuItem(OrganizationId organizationId, MenuId menuId, Menuitem managementMenuItem, Long orderIndex) {
    MenuItemId managementMenuItemId = new MenuItemId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));

    Identifier identifier = identifierController.findIdentifierById(managementMenuItemId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(menuId, orderIndex, managementMenuItemId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, menuId, orderIndex);
    }
    
    MenuItem menuItem = translateMenuItem(organizationId, managementMenuItem);
    MenuItemId kuntaApiMenuItemId = new MenuItemId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
        
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menuItem));
    menuItemCache.put(new IdPair<MenuId, MenuItemId>(menuId,kuntaApiMenuItemId), menuItem);
    
    return kuntaApiMenuItemId;
  }

  private Menu translateMenu(OrganizationId organizationId, fi.metatavu.management.client.model.Menu managementMenu) {
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
  
  private MenuItem translateMenuItem(OrganizationId organizationId, fi.metatavu.management.client.model.Menuitem managementMenuItem) {
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

  private MenuItemType getItemType(fi.metatavu.management.client.model.Menuitem managementMenuItem) {
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
  
  private void deleteMenu(OrganizationId organizationId, MenuId managementMenuId) {
    Identifier menuIdentifier = identifierController.findIdentifierById(managementMenuId);
    if (menuIdentifier != null) {
      MenuId kuntaApiMenuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, menuIdentifier.getKuntaApiId());
      queue.remove(managementMenuId);
      modificationHashCache.clear(menuIdentifier.getKuntaApiId());
      menuCache.clear(kuntaApiMenuId);
      identifierController.deleteIdentifier(menuIdentifier);
    }
  }
  
  private void deleteMenuItem(MenuId kuntaApiMenuId, MenuItemId kuntaApiMenuItemId) {
    Identifier menuItemIdentifier = identifierController.findIdentifierById(kuntaApiMenuItemId);
    if (menuItemIdentifier != null) {
      MenuItemId managementMenuItemId = idController.translateMenuItemId(kuntaApiMenuItemId, ManagementConsts.IDENTIFIER_NAME);
      queue.remove(managementMenuItemId);
      modificationHashCache.clear(menuItemIdentifier.getKuntaApiId());
      menuItemCache.clear(new IdPair<MenuId, MenuItemId>(kuntaApiMenuId, kuntaApiMenuItemId));
      identifierController.deleteIdentifier(menuItemIdentifier);
    }
  }
  
}
