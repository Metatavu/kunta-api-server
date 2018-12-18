package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.MenuProvider;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementMenuItemResourceContainer;
import fi.metatavu.kuntaapi.server.resources.MenuResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Menu provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementMenuProvider extends AbstractManagementProvider implements MenuProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private MenuResourceContainer menuCache;
  
  @Inject
  private ManagementMenuItemResourceContainer managementMenuItemResourceContainer;

  @Override
  public List<Menu> listOrganizationMenus(OrganizationId organizationId, String slug) {
    List<MenuId> menuIds = identifierRelationController.listMenuIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
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
      MenuItem menuItem = managementMenuItemResourceContainer.get(menuItemId);
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
    
    return managementMenuItemResourceContainer.get(menuItemId);
  }

}
