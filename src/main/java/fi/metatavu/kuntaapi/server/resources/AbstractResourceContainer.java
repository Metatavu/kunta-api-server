package fi.metatavu.kuntaapi.server.resources;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.kuntaapi.server.controllers.StoredResourceController;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.jackson.IdModule;
import fi.metatavu.kuntaapi.server.jackson.UnknownFieldLoggerDeserializationProblemHandler;

public abstract class AbstractResourceContainer<K extends BaseId, V> extends AbstractResourceContainerBase implements Serializable {
  
  private static final long serialVersionUID = 1744385470271720259L;

  @Inject
  private Logger logger;
  
  @Inject
  private StoredResourceController storedResourceController;
  
  public void put(K id, V response) {
    if (response == null) {
      clear(id);
    } else {
      String json = toJSON(response);
      if (json != null) {
        storedResourceController.updateData(getEntityType(), id, json);
      } else {
        if (logger.isLoggable(Level.SEVERE)) {
          logger.log(Level.SEVERE, String.format("Failed to serialize resource with id %s", id));
        }
      }
    }
  }
  
  public V get(K id) {
    String storedData = storedResourceController.getData(getEntityType(), id);
    if (storedData != null) {
      return fromJSON(storedData);
    }
    
    return null;
  }

  public void clear(K id) {
    storedResourceController.updateData(getEntityType(), id, null);
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
    objectMapper.addHandler(new UnknownFieldLoggerDeserializationProblemHandler());
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
  
}
