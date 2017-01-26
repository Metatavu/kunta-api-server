package fi.otavanopisto.kuntaapi.server.integrations.casem.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractFileCache;

@ApplicationScoped
public class CasemFileCache extends AbstractFileCache {

  private static final long serialVersionUID = 4993959656647694079L;

  @Override
  public String getCacheName() {
    return "casem-files";
  }

}
