package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.kuntaapi.server.cache.MenuCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementMenuItemCache;

/**
 * Menu provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementMenuProvider extends AbstractManagementProvider implements MenuProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private MenuCache menuCache;
  
  @Inject
  private ManagementMenuItemCache managementMenuItemCache;

  @Override
  public List<Menu> listOrganizationMenus(OrganizationId organizationId, String slug) {
    List<MenuId> menuIds = menuCache.getOragnizationIds(organizationId);
    List<Menu> menus = new ArrayList<>(menuIds.size());
    
    for (MenuId menuId : menuIds) {
      Menu menu = menuCache.get(menuId);
      if (menu != null && (slug == null || StringUtils.equals(slug, menu.getSlug()))) {
        menus.add(menu);
      }
    }
    
    return menus;
  }
  
  @Override
  public Menu findOrganizationMenu(OrganizationId organizationId, MenuId menuId) {
    return menuCache.get(menuId);
  }
  
  @Override
  public List<MenuItem> listOrganizationMenuItems(OrganizationId organizationId, MenuId menuId) {
    if (menuId == null) {
      return Collections.emptyList();
    }
    
    List<MenuItemId> menuItemIds = identifierRelationController.listMenuItemIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, menuId);
    List<MenuItem> result = new ArrayList<>(menuItemIds.size());
    
    for (MenuItemId menuItemId : menuItemIds) {
      MenuItem menuItem = managementMenuItemCache.get(menuItemId);
      if (menuItem != null) {
        result.add(menuItem);
      }
    }
    
    return result;
  }

  @Override
  public MenuItem findOrganizationMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId) {
    if (menuId == null || menuItemId == null) {
      return null;
    }
    
    if (!identifierRelationController.isChildOf(menuId, menuItemId)) {
      return null;
    }
    
    return managementMenuItemCache.get(menuItemId);
  }

}
