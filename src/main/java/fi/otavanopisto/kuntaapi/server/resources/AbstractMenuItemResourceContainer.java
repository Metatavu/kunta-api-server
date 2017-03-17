package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;

@ApplicationScoped
public abstract class AbstractMenuItemResourceContainer extends AbstractResourceContainer<MenuItemId, MenuItem> {

  private static final long serialVersionUID = -1830491083897405955L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
