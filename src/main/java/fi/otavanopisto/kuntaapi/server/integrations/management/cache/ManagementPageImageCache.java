package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageImageCache;

@ApplicationScoped
public class ManagementPageImageCache extends AbstractPageImageCache {

  private static final long serialVersionUID = 1238989224470948401L;

  @Override
  public String getCacheName() {
    return "management-page-images";
  }

}
