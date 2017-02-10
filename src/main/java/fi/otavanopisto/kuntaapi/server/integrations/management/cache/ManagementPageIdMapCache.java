package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageIdMapCache;

@ApplicationScoped
public class ManagementPageIdMapCache extends AbstractPageIdMapCache {

  private static final long serialVersionUID = 6300784323817741857L;

  @Override
  public String getCacheName() {
    return "management-page-map-cache";
  }

}
