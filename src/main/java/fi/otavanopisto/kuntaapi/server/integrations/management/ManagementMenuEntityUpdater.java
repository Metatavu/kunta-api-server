package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menuitem;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider.MenuItemType;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementMenuItemResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.MenuIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.resources.MenuResourceContainer;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class ManagementMenuEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private MenuIdTaskQueue menuIdTaskQueue;
  
  @Inject
  private IdController idController;
  
  @Inject
  private MenuResourceContainer menuCache;

  @Inject
  private ManagementMenuItemResourceContainer menuItemCache;
  
  @Override
  public String getName() {
    return "management-menus";
  }
  
  @Override
  public void timeout() {
    executeNextTask();
  }
  
  private void executeNextTask() {
    IdTask<MenuId> task = menuIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementMenu(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteManagementMenu(task.getId());
      }
    }
  }

  private void updateManagementMenu(MenuId managementMenuId, Long orderIndex) {
    OrganizationId organizationId = managementMenuId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<fi.metatavu.management.client.model.Menu> response = api.kuntaApiMenusIdGet(managementMenuId.getId());
    if (response.isOk()) {
      updateManagementMenu(api, organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Finding organization %s menu failed on [%d] %s", managementMenuId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateManagementMenu(DefaultApi api, OrganizationId organizationId, fi.metatavu.management.client.model.Menu managementMenu, Long orderIndex) {
    Identifier menuIdentifier = updateManagementMenu(organizationId, managementMenu, orderIndex);
    MenuId menuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, menuIdentifier.getKuntaApiId());
    List<MenuItemId> existingKuntaApiMenuItemIds = identifierRelationController.listMenuItemIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, menuId);
    List<Menuitem> managementMenuItems = listManagementMenuItems(api, managementMenu);
    for (int i = 0, l = managementMenuItems.size(); i < l; i++) {
      Menuitem managementMenuItem = managementMenuItems.get(i);
      MenuItemId menuItemId = updateManagementMenuItem(organizationId, menuIdentifier, managementMenuItem, (long) i);
      existingKuntaApiMenuItemIds.remove(menuItemId);
    }
    
    for (MenuItemId existingKuntaApiMenuItemId : existingKuntaApiMenuItemIds) {
      deleteMenuItem(existingKuntaApiMenuItemId);
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
  
  private Identifier updateManagementMenu(OrganizationId organizationId, fi.metatavu.management.client.model.Menu managementMenu, Long orderIndex) {
    MenuId managementMenuId = new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementMenuId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    MenuId kuntaApiMenuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Menu menu = translateMenu(organizationId, managementMenu);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menu));
    menuCache.put(kuntaApiMenuId, menu);
    
    return identifier;
  }
  
  private MenuItemId updateManagementMenuItem(OrganizationId organizationId, Identifier menuIdentifier, Menuitem managementMenuItem, Long orderIndex) {
    MenuItemId managementMenuItemId = new MenuItemId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementMenuItemId);
    identifierRelationController.setParentIdentifier(identifier, menuIdentifier);

    MenuItemId kuntaApiMenuItemId = new MenuItemId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    MenuItem menuItem = translateMenuItem(organizationId, managementMenuItem);
        
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(menuItem));
    menuItemCache.put(kuntaApiMenuItemId, menuItem);
    
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
  
  private void deleteManagementMenu(MenuId managementMenuId) {
    OrganizationId organizationId = managementMenuId.getOrganizationId();
    
    List<MenuItemId> menuItemIds = identifierRelationController.listMenuItemIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, managementMenuId);
    for (MenuItemId menuItemId : menuItemIds) {
      deleteMenuItem(menuItemId);
    }
    
    Identifier menuIdentifier = identifierController.findIdentifierById(managementMenuId);
    if (menuIdentifier != null) {
      MenuId kuntaApiMenuId = new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, menuIdentifier.getKuntaApiId());
      modificationHashCache.clear(menuIdentifier.getKuntaApiId());
      menuCache.clear(kuntaApiMenuId);
      identifierController.deleteIdentifier(menuIdentifier);
    }
  }
  
  private void deleteMenuItem(MenuItemId kuntaApiMenuItemId) {
    Identifier menuItemIdentifier = identifierController.findIdentifierById(kuntaApiMenuItemId);
    if (menuItemIdentifier != null) {
      modificationHashCache.clear(menuItemIdentifier.getKuntaApiId());
      menuItemCache.clear(kuntaApiMenuItemId);
      identifierController.deleteIdentifier(menuItemIdentifier);
    }
  }
  
}
