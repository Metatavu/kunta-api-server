package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.MenuCache;
import fi.otavanopisto.kuntaapi.server.cache.MenuItemCache;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Menuitem;

/**
 * Menu provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementMenuProvider extends AbstractManagementProvider implements MenuProvider {
  
  private static final String COULD_NOT_TRANSLATE_MENU_ID = "Could not translate menu id %s to management id";

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private MenuCache menuCache;
  
  @Inject
  private MenuItemCache menuItemCache;

  @Inject
  private IdController idController;

  @Override
  public List<Menu> listOrganizationMenus(OrganizationId organizationId, String slug) {
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menu>> response = 
        managementApi.getApi(organizationId).kuntaApiMenusGet(slug);
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      
      List<fi.otavanopisto.mwp.client.model.Menu> managementMenus = response.getResponse();
      List<MenuId> menuIds = new ArrayList<>(managementMenus.size());
      
      for (fi.otavanopisto.mwp.client.model.Menu managementMenu : managementMenus) {
        menuIds.add(new MenuId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId())));
      }
      
      return getCachedMenus(menuIds);
    }
    
    return Collections.emptyList();
  }
  
  @Override
  public Menu findOrganizationMenu(OrganizationId organizationId, MenuId menuId) {
    if (menuId == null) {
      return null;
    }
    
    MenuId managemenMenuId = idController.translateMenuId(menuId, ManagementConsts.IDENTIFIER_NAME);
    if (managemenMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    return menuCache.get(managemenMenuId);
  }
  
  @Override
  public List<MenuItem> listOrganizationMenuItems(OrganizationId organizationId, MenuId menuId) {
    if (menuId == null) {
      return Collections.emptyList();
    }
    
    MenuId managementMenuId = idController.translateMenuId(menuId, ManagementConsts.IDENTIFIER_NAME);
    if (managementMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menuitem>> response = managementApi.getApi(organizationId)
        .kuntaApiMenusMenuIdItemsGet(managementMenuId.getId());

    if (!response.isOk()) {
      logger.severe(String.format("Menu item listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<Menuitem> managementMenuItems = response.getResponse();
      List<IdPair<MenuId, MenuItemId>> menuItemIds = new ArrayList<>(managementMenuItems.size());
      
      for (fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem : managementMenuItems) {
        MenuItemId menuItemId = new MenuItemId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));
        menuItemIds.add(new IdPair<MenuId, MenuItemId>(menuId, menuItemId));
      }
      
      return getCachedItems(menuItemIds);
    }
    
    return Collections.emptyList();
  }

  @Override
  public MenuItem findOrganizationMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId) {
    if (menuId == null || menuItemId == null) {
      return null;
    }
    
    MenuId managementMenuId = idController.translateMenuId(menuId, ManagementConsts.IDENTIFIER_NAME);
    if (managementMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    MenuItemId managementItemId = idController.translateMenuItemId(menuItemId, ManagementConsts.IDENTIFIER_NAME);
    if (managementItemId == null) {
      logger.severe(String.format("Could not translate menu item id %s to management id", menuItemId.toString()));
      return null;
    }
    
    return menuItemCache.get(new IdPair<MenuId, MenuItemId>(managementMenuId, managementItemId));
  }

  private List<Menu> getCachedMenus(List<MenuId> menuIds) {
    List<Menu> result = new ArrayList<>(menuIds.size());
        
    for (MenuId menuId : menuIds) {
      result.add(menuCache.get(menuId));
    }
    
    return result;
  }

  private List<MenuItem> getCachedItems(List<IdPair<MenuId, MenuItemId>> childIds) {
    List<MenuItem> result = new ArrayList<>(childIds.size());
    
    for (IdPair<MenuId, MenuItemId> childId : childIds) {
      result.add(menuItemCache.get(childId));
    }
    
    return result;
  }
  
}
