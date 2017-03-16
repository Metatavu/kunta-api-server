package fi.otavanopisto.kuntaapi.server.cache;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.PersistenceConfiguration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.manager.CacheContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.otavanopisto.kuntaapi.server.jackson.IdModule;

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
  
  /**
   * Returns cache's name
   * 
   * @return cache's name
   */
  public abstract String getCacheName();
  
  /**
   * Returns whether cache is expected to be stored
   * 
   * @return whether cache is expected to be stored
   */
  public abstract boolean isStored();

  public Cache<K, String> getCache() {
    return cacheContainer.getCache(getCacheName());
  }
  
  public boolean isHealthy() {
    Configuration configuration = getCache().getCacheConfiguration();
    if (configuration == null) {
      logger.log(Level.WARNING, "Cache %s is not configured");
      return false;
    }
    
    if (isStored()) {
      PersistenceConfiguration persistence = configuration.persistence();
      if (persistence == null) {
        logger.log(Level.WARNING, "Cache %s has no persistence configured");
        return false;
      }
      
      List<StoreConfiguration> stores = persistence.stores();
      if (stores.isEmpty()) {
        logger.log(Level.WARNING, () -> String.format("Cache %s has no stored configured", getCacheName()));
        return false;
      }
    }
    
    return true;
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
   * Removes elements from the cache
   * 
   * @param id entity id
   */
  public void clear(K id) {
    Cache<K, String> cache = getCache();
    cache.remove(id);
  }
  
  protected Type[] getParameterizedTypeArguments() {
    ParameterizedType parameterizedTypeClass = getParameterizedType();
    if (parameterizedTypeClass != null) {
      return parameterizedTypeClass.getActualTypeArguments();
    }
    
    return new Type[0];
  }
  
  private ParameterizedType getParameterizedType() {
    Class<?> currentClass = getClass();
    while (currentClass != null && !currentClass.equals(Object.class))  {
      if (currentClass.getGenericSuperclass() instanceof ParameterizedType) {
        return (ParameterizedType) currentClass.getGenericSuperclass();
      }
      
      currentClass = currentClass.getSuperclass();
    }
    
    return null;
  }
  
  protected V fromJSON(String rawData) {
    try {
      return getObjectMapper().readValue(rawData, getTypeReference());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Could not unserialize data", e);
    }
    
    return null;
  }
  
  protected String toJSON(V value) {
    try {
      return getObjectMapper().writeValueAsString(value);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize entity", e);
    }
    
    return null;
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new IdModule());
    return objectMapper;
  }
  
  private TypeReference<V> getTypeReference() {    
    final Type[] parameterizedTypeArguments = getParameterizedTypeArguments();
    
    return new TypeReference<V>() {
      @Override
      public Type getType() {
        return parameterizedTypeArguments[parameterizedTypeArguments.length - 1];
      }
    };
  }
  
}
