package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ModificationHashCache extends AbstractCache<String, String> {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Override
  public String getCacheName() {
    return "modificationhash";
  }
  
  @Override
  public boolean isStored() {
    return false;
  }
  
}
