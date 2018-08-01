package fi.metatavu.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractMenuItemResourceContainer;

@ApplicationScoped
public class ManagementMenuItemResourceContainer extends AbstractMenuItemResourceContainer {

  private static final long serialVersionUID = -4416805402225855726L;

  @Override
  public String getName() {
    return "management-menu-items";
  }
  
}
