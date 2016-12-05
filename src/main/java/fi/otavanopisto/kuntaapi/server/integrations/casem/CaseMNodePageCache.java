package fi.otavanopisto.kuntaapi.server.integrations.casem;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractCache;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;

@ApplicationScoped
@Singleton
public class CaseMNodePageCache extends AbstractCache<String, Page> {

  private static final long serialVersionUID = -6823625136135832573L;

  @Override
  public String getCacheName() {
    return "casem-page-cache";
  }
  
  
}
