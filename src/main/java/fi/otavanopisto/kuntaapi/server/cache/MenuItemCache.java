package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;

@ApplicationScoped
public class MenuItemCache extends AbstractEntityRelationCache<MenuId, MenuItemId, MenuItem> {
 
  private static final long serialVersionUID = 7185610210122036355L;

  @Override
  public String getCacheName() {
    return "menu-items";
  }
  
}
