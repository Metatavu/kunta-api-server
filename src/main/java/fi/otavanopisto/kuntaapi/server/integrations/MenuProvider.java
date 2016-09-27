package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;

/**
 * Interafce that describes a menu provider
 * 
 * @author Antti Leppä
 */
public interface MenuProvider {

  /**
   * List menus in an organization
   * 
   * @param organizationId organization id
   * @param filter by slug
   * @return list of organization menus
   */
  public List<Menu> listOrganizationMenus(OrganizationId organizationId, String slug);
  
  /**
   * Finds a single organization menu
   * 
   * @param organizationId organization id
   * @param menuId menu id
   * @return menu or null of not found
   */
  public Menu findOrganizationMenu(OrganizationId organizationId, MenuId menuId);
  
  /**
   * List menu items
   * 
   * @param organizationId organization id
   * @param menuId menuId
   * @return list of organization menu items
   */
  public List<MenuItem> listOrganizationMenuItems(OrganizationId organizationId, MenuId menuId);
  
  /**
   * Find a menu item
   * 
   * @param organizationId organization id
   * @param menuId menuId
   * @param menuItemId menuItemId
   * @return Find single menu item
   */
  public MenuItem findOrganizationMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId);
  
  /**
   * Enumeration describing a menu item type
   */
  public enum MenuItemType {
    
    PAGE,
    
    NEWS_ARTICLE,
    
    FILE,
    
    LINK
    
  }
  
}
