package fi.otavanopisto.kuntaapi.server.integrations.casem.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageContentCache;

@ApplicationScoped
public class CaseMPageContentCache extends AbstractPageContentCache {

  private static final long serialVersionUID = 5074106733901932323L;

  @Override
  public String getCacheName() {
    return "casem-page-contents";
  }

}
