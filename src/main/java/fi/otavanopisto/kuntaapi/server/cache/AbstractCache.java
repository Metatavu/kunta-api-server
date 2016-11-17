package fi.otavanopisto.kuntaapi.server.cache;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Abstract base cache for all entity caches
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@SuppressWarnings ({"squid:S3306", "squid:S1948"})
public abstract class AbstractCache <K, V> implements Serializable {
  
  private static final long serialVersionUID = 4458370063943309700L;

  @Inject
  private Logger logger;

  @Resource (lookup = "java:jboss/infinispan/container/kunta-api")
  private CacheContainer cacheContainer;
  
  public abstract String getCacheName();

  public Cache<K, String> getCache() {
    return cacheContainer.getCache(getCacheName());
  }
  
  /**
   * Returns cached entity by id
   * 
   * @param id entity id
   * @return cached api reposponse or null if non found
   */
  public V get(K id) {
    Cache<K, String> cache = getCache();
    if (cache.containsKey(id)) {
      String rawData = cache.get(id);
      if (rawData == null) {
        logger.log(Level.SEVERE, String.format("Could not find data for id %s", id));
        return null;
      }
      
      try {
        return getObjectMapper().readValue(rawData, getTypeReference());
      } catch (IOException e) {
        cache.remove(id);
        logger.log(Level.SEVERE, "Invalid serizalized object found from the cache. Dropped object", e);
      }
    }
    
    return null;
  }
  
  /**
   * Caches an entity
   * 
   * @param id entity id
   * @param response
   */
  public void put(K id, V response) {
    Cache<K, String> cache = getCache();
    try {
      cache.put(id, getObjectMapper().writeValueAsString(response));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize response into cache", e);
    }
  }
  
  /**
   * Returns all cached ids
   * 
   * @return  all cached ids
   */
  public Set<K> getIds() {
    Cache<K, String> cache = getCache();
    return cache.keySet();
  }
  
  /**
   * Removes elements from the cache
   * 
   * @param id entity id
   */
  public void clear(String id) {
    Cache<K, String> cache = getCache();
    cache.remove(id);
  }
  
  protected Type[] getParameterizedTypes() {
    Type superClass = getClass().getGenericSuperclass();
    if (superClass instanceof ParameterizedType) {
      return ((ParameterizedType) superClass).getActualTypeArguments();
    }
    
    return new Type[0];
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  
  private TypeReference<V> getTypeReference() {    
    final Type[] parameterizedTypes = getParameterizedTypes();
    
    return new TypeReference<V>() {
      @Override
      public Type getType() {
        return parameterizedTypes[parameterizedTypes.length - 1];
      }
    };
  }
  
}
