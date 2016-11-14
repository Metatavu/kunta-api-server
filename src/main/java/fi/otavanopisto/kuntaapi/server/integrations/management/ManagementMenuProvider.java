package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.AbstractMwpProvider;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.MwpConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.mwp.client.ApiResponse;

/**
 * Menu provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementMenuProvider extends AbstractMwpProvider implements MenuProvider {
  
  private static final String COULD_NOT_TRANSLATE_MENU_ID = "Could not translate menu id %s to management id";

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;

  @Override
  public List<Menu> listOrganizationMenus(OrganizationId organizationId, String slug) {
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menu>> response = 
        managementApi.getApi(organizationId).kuntaApiMenusGet(slug);
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateMenus(response.getResponse());
    }
    
    return Collections.emptyList();
  }
  
  @Override
  public Menu findOrganizationMenu(OrganizationId organizationId, MenuId menuId) {
    if (menuId == null) {
      return null;
    }
    
    MenuId managemenMenutId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (managemenMenutId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Menu> response = 
        managementApi.getApi(organizationId).kuntaApiMenusIdGet(managemenMenutId.getId());
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu finding failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateMenu(response.getResponse());
    }
    
    return null;
  }
  
  @Override
  public List<MenuItem> listOrganizationMenuItems(OrganizationId organizationId, MenuId menuId) {
    if (menuId == null) {
      return Collections.emptyList();
    }
    
    MenuId managementMenuId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (managementMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menuitem>> response = managementApi.getApi(organizationId)
        .kuntaApiMenusMenuIdItemsGet(managementMenuId.getId());
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu item listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateMenuItems(response.getResponse());
    }
    
    return Collections.emptyList();
  }
  
  @Override
  public MenuItem findOrganizationMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId) {
    if (menuId == null || menuItemId == null) {
      return null;
    }
    
    MenuId managementMenuId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (managementMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    MenuItemId managementItemId = idController.translateMenuItemId(menuItemId, MwpConsts.IDENTIFIER_NAME);
    if (managementItemId == null) {
      logger.severe(String.format("Could not translate menu item id %s to management id", menuItemId.toString()));
      return null;
    }
    
    return findMenuItem(organizationId, managementMenuId, managementItemId);
  }

  private MenuItem findMenuItem(OrganizationId organizationId, MenuId managementMenuId, MenuItemId managementItemId) {
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menuitem>> response =  
        managementApi.getApi(organizationId).kuntaApiMenusMenuIdItemsGet(managementMenuId.getId());
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu finding failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      for (fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem : response.getResponse()) {
        if (managementItemId.getId().equals(String.valueOf(managementMenuItem.getId()))) {
          return translateMenuItem(managementMenuItem);
        }
      }
    }
    
    return null;
  }
  
  private List<Menu> translateMenus(List<fi.otavanopisto.mwp.client.model.Menu> managementMenus) {
    List<Menu> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Menu managementMenu : managementMenus) {
      result.add(translateMenu(managementMenu));
    }
    
    return result;
  }

  private Menu translateMenu(fi.otavanopisto.mwp.client.model.Menu managementMenu) {
    Menu menu = new Menu();
    
    MenuId managementMenuId = new MenuId(MwpConsts.IDENTIFIER_NAME, String.valueOf(managementMenu.getId()));
    MenuId kuntaApiMenuId = idController.translateMenuId(managementMenuId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiMenuId == null) {
      logger.info(String.format("Could not translate management menu %d into kunta api id", managementMenu.getId()));
      return null;
    }
    
    menu.setId(kuntaApiMenuId.getId());
    menu.setSlug(managementMenu.getSlug());
    
    return menu;
  }
  
  private List<MenuItem> translateMenuItems(List<fi.otavanopisto.mwp.client.model.Menuitem> managementMenuItems) {
    List<MenuItem> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem : managementMenuItems) {
      result.add(translateMenuItem(managementMenuItem));
    }
    
    return result;
  }

  private MenuItem translateMenuItem(fi.otavanopisto.mwp.client.model.Menuitem managementMenuItem) {
    MenuItem menuItem = new MenuItem();
    
    MenuItemId managementMenuItemId = new MenuItemId(MwpConsts.IDENTIFIER_NAME, String.valueOf(managementMenuItem.getId()));
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
    
    PageId pageId = translatePageId(managementMenuItem.getPageId());
    MenuItemId parentMenuItemId = translateMenuItemId(managementMenuItem.getParentItemId());
    
    menuItem.setId(kuntaApiMenuItemId.getId());
    menuItem.setLabel(managementMenuItem.getTitle());
    menuItem.setFileId(null);
    menuItem.setExternalUrl(itemType == MenuItemType.LINK ? managementMenuItem.getUrl() : null);
    menuItem.setPageId(pageId != null ? pageId.getId() : null);
    menuItem.setParentItemId(parentMenuItemId != null ? parentMenuItemId.getId() : null);
    menuItem.setType(itemType.toString());
    
    return menuItem;
  }

  private MenuItemId translateMenuItemId(Long parentItemId) {
    if (parentItemId == null) {
      return null;
    }
    
    MenuItemId managementMenuItem = new MenuItemId(MwpConsts.IDENTIFIER_NAME, String.valueOf(parentItemId));

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

}
