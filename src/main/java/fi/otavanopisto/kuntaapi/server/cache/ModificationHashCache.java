package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ModificationHashCache extends AbstractEntityCache<String> {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Override
  public String getCacheName() {
    return "modificationhash";
  }
  
}
