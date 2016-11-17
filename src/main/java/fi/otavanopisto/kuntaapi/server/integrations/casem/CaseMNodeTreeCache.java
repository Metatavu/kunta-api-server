package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractCache;
import fi.otavanopisto.kuntaapi.server.id.PageId;

@ApplicationScoped
@Singleton
public class CaseMNodeTreeCache extends AbstractCache<String, List<PageId>> {

  private static final long serialVersionUID = -6823625136135832573L;

  @Override
  public String getCacheName() {
    return "casem-tree-cache";
  }
  
  
}
