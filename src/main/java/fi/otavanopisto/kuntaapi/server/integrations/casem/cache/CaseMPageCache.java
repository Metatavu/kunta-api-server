package fi.otavanopisto.kuntaapi.server.integrations.casem.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageCache;

@ApplicationScoped
public class CaseMPageCache extends AbstractPageCache {

  private static final long serialVersionUID = -5748105617264972537L;

  @Override
  public String getCacheName() {
    return "casem-pages";
  }

}
