package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Menu;

@ApplicationScoped
public class MenuCache extends AbstractResourceContainer<MenuId, Menu> {
  
  private static final long serialVersionUID = 7116646256910860395L;

  @Override
  public String getName() {
    return "menus";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
