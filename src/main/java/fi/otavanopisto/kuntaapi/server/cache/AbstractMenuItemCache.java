package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractMenuItemCache extends AbstractResourceContainer<MenuItemId, MenuItem> {

  private static final long serialVersionUID = -1830491083897405955L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
