package fi.metatavu.kuntaapi.server.cache;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import javax.ejb.Singleton;

import javax.annotation.Resource;

import javax.inject.Inject;

import org.infinispan.Cache;

@ApplicationScoped
@Singleton
public class ModificationHashCache {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Inject
  private Logger logger;

  @Resource(lookup = "java:jboss/infinispan/cache/kunta-api/modificationhash")
  private Cache<String, String> cache; 

  /**
   * Returns cached entity by id
   * 
   * @param id entity id
   * @return cached api reposponse or null if non found
   */
  public String get(String id) {
    if (cache.containsKey(id)) {
      String rawData = cache.get(id);
      if (rawData == null) {
        logger.log(Level.SEVERE, () -> String.format("Could not find data for id %s", id));
        return null;
      }
      
      return rawData;
    }
    
    return null;
  }
  
  /**
   * Caches an entity
   * 
   * @param id entity id
   * @param response
   */
  public void put(String id, String response) {
    if (response == null) {
      clear(id);
    } else {
      cache.put(id, response);
    }
  }
  
  /**
   * Removes elements from the cache
   * 
   * @param id entity id
   */
  public void clear(String id) {
    cache.remove(id);
  }

}
