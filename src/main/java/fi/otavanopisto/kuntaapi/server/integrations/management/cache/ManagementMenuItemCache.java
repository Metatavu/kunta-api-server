package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractMenuItemResourceContainer;

@ApplicationScoped
public class ManagementMenuItemCache extends AbstractMenuItemResourceContainer {

  private static final long serialVersionUID = -4416805402225855726L;

  @Override
  public String getName() {
    return "management-menu-items";
  }
  
}
