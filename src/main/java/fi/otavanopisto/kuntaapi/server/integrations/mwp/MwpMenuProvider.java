package fi.otavanopisto.kuntaapi.server.integrations.mwp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementApi;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.mwp.client.ApiResponse;

/**
 * Menu provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class MwpMenuProvider extends AbstractMwpProvider implements MenuProvider {
  
  private static final String COULD_NOT_TRANSLATE_MENU_ID = "Could not translate menu id %s to MWP id";

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
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
    
    MenuId mwpId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (mwpId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Menu> response = 
        managementApi.getApi(organizationId).kuntaApiMenusIdGet(mwpId.getId());
    
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
    
    MenuId mwpId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (mwpId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menuitem>> response = managementApi.getApi(organizationId)
        .kuntaApiMenusMenuIdItemsGet(mwpId.getId());
    
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
    
    MenuId mwpMenuId = idController.translateMenuId(menuId, MwpConsts.IDENTIFIER_NAME);
    if (mwpMenuId == null) {
      logger.severe(String.format(COULD_NOT_TRANSLATE_MENU_ID, menuId.toString()));
      return null;
    }
    
    MenuItemId mwpItemId = idController.translateMenuItemId(menuItemId, MwpConsts.IDENTIFIER_NAME);
    if (mwpItemId == null) {
      logger.severe(String.format("Could not translate menu item id %s to MWP id", menuItemId.toString()));
      return null;
    }
    
    return findMenuItem(organizationId, mwpMenuId, mwpItemId);
  }

  private MenuItem findMenuItem(OrganizationId organizationId, MenuId mwpMenuId, MenuItemId mwpItemId) {
    ApiResponse<List<fi.otavanopisto.mwp.client.model.Menuitem>> response =  
        managementApi.getApi(organizationId).kuntaApiMenusMenuIdItemsGet(mwpMenuId.getId());
    
    if (!response.isOk()) {
      logger.severe(String.format("Menu finding failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      for (fi.otavanopisto.mwp.client.model.Menuitem mwpItem : response.getResponse()) {
        if (mwpItemId.getId().equals(String.valueOf(mwpItem.getId()))) {
          return translateMenuItem(mwpItem);
        }
      }
    }
    
    return null;
  }
  
  private List<Menu> translateMenus(List<fi.otavanopisto.mwp.client.model.Menu> mwpMenus) {
    List<Menu> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Menu mwpMenu : mwpMenus) {
      result.add(translateMenu(mwpMenu));
    }
    
    return result;
  }

  private Menu translateMenu(fi.otavanopisto.mwp.client.model.Menu mwpMenu) {
    Menu menu = new Menu();
    
    MenuId mwpId = new MenuId(MwpConsts.IDENTIFIER_NAME, String.valueOf(mwpMenu.getId()));
    MenuId kuntaApiId = idController.translateMenuId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new menu %d", mwpMenu.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(mwpId);
      kuntaApiId = new MenuId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    menu.setId(kuntaApiId.getId());
    menu.setSlug(mwpMenu.getSlug());
    
    return menu;
  }
  
  private List<MenuItem> translateMenuItems(List<fi.otavanopisto.mwp.client.model.Menuitem> mwpMenuItems) {
    List<MenuItem> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Menuitem mwpMenuItem : mwpMenuItems) {
      result.add(translateMenuItem(mwpMenuItem));
    }
    
    return result;
  }

  private MenuItem translateMenuItem(fi.otavanopisto.mwp.client.model.Menuitem mwpMenuItem) {
    MenuItem menuItem = new MenuItem();
    
    MenuItemId mwpId = new MenuItemId(MwpConsts.IDENTIFIER_NAME, String.valueOf(mwpMenuItem.getId()));
    MenuItemId kuntaApiId = idController.translateMenuItemId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new menu item %d", mwpMenuItem.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(mwpId);
      kuntaApiId = new MenuItemId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    MenuItemType itemType = getItemType(mwpMenuItem);
    if (itemType == null) {
      logger.severe(String.format("Could not determine item type for %d", mwpMenuItem.getId()));
      return null;
    }
    
    PageId pageId = translatePageId(mwpMenuItem.getPageId());
    MenuItemId parentMenuItemId = translateMenuItemId(mwpMenuItem.getParentItemId());
    
    menuItem.setId(kuntaApiId.getId());
    menuItem.setLabel(mwpMenuItem.getTitle());
    menuItem.setFileId(null);
    menuItem.setExternalUrl(itemType == MenuItemType.LINK ? mwpMenuItem.getUrl() : null);
    menuItem.setPageId(pageId != null ? pageId.getId() : null);
    menuItem.setParentItemId(parentMenuItemId != null ? parentMenuItemId.getId() : null);
    menuItem.setType(itemType.toString());
    
    return menuItem;
  }

  private MenuItemId translateMenuItemId(Long parentItemId) {
    if (parentItemId == null) {
      return null;
    }
    
    MenuItemId mwpMenuItem = new MenuItemId(MwpConsts.IDENTIFIER_NAME, String.valueOf(parentItemId));

    return idController.translateMenuItemId(mwpMenuItem, KuntaApiConsts.IDENTIFIER_NAME);
  }

  private MenuItemType getItemType(fi.otavanopisto.mwp.client.model.Menuitem mwpMenuItem) {
    switch (mwpMenuItem.getType()) {
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
