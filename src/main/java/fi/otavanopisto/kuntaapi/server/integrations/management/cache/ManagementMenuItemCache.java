package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractMenuItemCache;

@ApplicationScoped
public class ManagementMenuItemCache extends AbstractMenuItemCache {

  private static final long serialVersionUID = -4416805402225855726L;

  @Override
  public String getCacheName() {
    return "management-menu-items";
  }
  
}
