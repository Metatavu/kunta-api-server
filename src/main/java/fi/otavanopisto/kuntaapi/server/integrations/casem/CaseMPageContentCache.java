package fi.otavanopisto.kuntaapi.server.integrations.casem;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractCache;

@ApplicationScoped
@Singleton
public class CaseMPageContentCache extends AbstractCache<String, String> {

  private static final long serialVersionUID = 2161788338311732033L;

  @Override
  public String getCacheName() {
    return "casem-content-cache";
  }
  
}
