package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.rest.model.Menu;

@ApplicationScoped
public class MenuResourceContainer extends AbstractResourceContainer<MenuId, Menu> {
  
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
