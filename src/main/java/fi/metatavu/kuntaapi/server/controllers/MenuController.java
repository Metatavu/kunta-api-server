package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.MenuProvider;
import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class MenuController {

  @Inject
  private EntityController entityController;

  @Inject
  private Instance<MenuProvider> menuProviders;
  
  public List<Menu> listMenus(String slug, OrganizationId organizationId) {
    List<Menu> result = new ArrayList<>();
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      result.addAll(menuProvider.listOrganizationMenus(organizationId, slug));
    }
    
    return entityController.sortEntitiesInNaturalOrder(result);
  }

  public Menu findMenu(OrganizationId organizationId, MenuId menuId) {
    for (MenuProvider menuProvider : getMenuProviders()) {
      Menu menu = menuProvider.findOrganizationMenu(organizationId, menuId);
      if (menu != null) {
        return menu;
      }
    }
    
    return null;
  }

  public List<MenuItem> listMenuItems(OrganizationId organizationId, MenuId menuId) {
    List<MenuItem> result = new ArrayList<>();
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      result.addAll(menuProvider.listOrganizationMenuItems(organizationId, menuId));
    }
    
    return entityController.sortEntitiesInNaturalOrder(result);
  }
  
  public MenuItem findMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId) {
    for (MenuProvider menuProvider : getMenuProviders()) {
      MenuItem menuItem = menuProvider.findOrganizationMenuItem(organizationId, menuId, menuItemId);
      if (menuItem != null) {
        return menuItem;
      }
    }
    
    return null;
  }
  
  private List<MenuProvider> getMenuProviders() {
    List<MenuProvider> result = new ArrayList<>();
    
    Iterator<MenuProvider> iterator = menuProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
