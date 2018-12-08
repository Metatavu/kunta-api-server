package fi.metatavu.kuntaapi.server.rest.ptv7adapter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;

/**
 * Ptv7 adapter
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class Ptv7Adapter {
  
  @Inject
  private Logger logger;
  
  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public Object translate(Object entity) throws IOException {
    if (entity instanceof Organization) {
      return translate((Organization) entity);
    } else if (entity instanceof ElectronicServiceChannel) {
      return translate((ElectronicServiceChannel) entity);
    } else if (entity instanceof PrintableFormServiceChannel) {
      return translate((PrintableFormServiceChannel) entity);
    } else if (entity instanceof ServiceLocationServiceChannel) {
      return translate((ServiceLocationServiceChannel) entity);
    } else if (entity instanceof PhoneServiceChannel) {
      return translate((PhoneServiceChannel) entity);
    } else if (entity instanceof WebPageServiceChannel) {
      return translate((WebPageServiceChannel) entity);
    } else if (entity instanceof Service) {
      return translate((Service) entity);
    }
    
    return entity;
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public Organization translate(Organization entity) throws IOException {
    return translateEntity(Organization.class, cloneEntity(Organization.class, entity), getMappings().getOrganization());
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public ElectronicServiceChannel translate(ElectronicServiceChannel entity) throws IOException {
    return translateEntity(ElectronicServiceChannel.class, cloneEntity(ElectronicServiceChannel.class, entity), getMappings().getElectoricServiceChannel());
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public PrintableFormServiceChannel translate(PrintableFormServiceChannel entity) throws IOException {
    return translateEntity(PrintableFormServiceChannel.class, cloneEntity(PrintableFormServiceChannel.class, entity), getMappings().getPrintableFormServiceChannel());
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public ServiceLocationServiceChannel translate(ServiceLocationServiceChannel entity) throws IOException {
    return translateEntity(ServiceLocationServiceChannel.class, cloneEntity(ServiceLocationServiceChannel.class, entity), getMappings().getServiceLocationServiceChannel());
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public PhoneServiceChannel translate(PhoneServiceChannel entity) throws IOException {
    return translateEntity(PhoneServiceChannel.class, cloneEntity(PhoneServiceChannel.class, entity), getMappings().getPhoneServiceChannel());
  }

  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public WebPageServiceChannel translate(WebPageServiceChannel entity) throws IOException {
    return translateEntity(WebPageServiceChannel.class, cloneEntity(WebPageServiceChannel.class, entity), getMappings().getWebPageServiceChannel());
  }
  
  /**
   * Translates PTV8 to use PTV7 naming conventions
   * 
   * @param entity entity
   * @return translated entity
   * @throws IOException thrown on reading error
   */
  public Service translate(Service entity) throws IOException {
    return translateEntity(Service.class, cloneEntity(Service.class, entity), getMappings().getService());
  }
  
  /**
   * Applies PTV8 -> PTV7 naming convention into an object 
   * 
   * @param entityClass entity class
   * @param entity entity
   * @param entityMapping entity mapping
   * @return cloned entity using PTV7 naming convention
   * @throws IOException thrown on reading error
   */
  private <T> T translateEntity(Class<T> entityClass, T entity, Map<String, Map<String, String>> entityMapping) throws IOException {
    if (entity == null) {
      return null;
    }
    
    T result = cloneEntity(entityClass, entity);
    
    entityMapping.keySet().forEach(propertyPath -> {
      Map<String, String> propertyMapping = entityMapping.get(propertyPath);
      try {
        applyPropertyValue(result, new ArrayList<>(Arrays.asList(StringUtils.split(propertyPath, "/"))), propertyMapping);
      } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
        logger.log(Level.WARNING, "Failed to apply PTV 7 compatibility changes", e);
      }
    });
    
    return result;
  }
  
  /**
   * Clones entity
   * 
   * @param entityClass entity class
   * @param entity entity
   * @return cloned entity
   * @throws IOException thrown when entity reading fails
   */
  private <T> T cloneEntity(Class<T> entityClass, T entity) throws IOException {
    if (entity == null) {
      return null;
    }
    
    ObjectMapper objectMapper = getObjectMapper();
    return objectMapper.readValue(objectMapper.writeValueAsBytes(entity), entityClass);
  }

  /**
   * Applies PTV8 -> PTV7 naming convention into an object 
   * 
   * @param bean object
   * @param propertyPath property path
   * @param propertyMapping property mapping 
   * @throws IllegalAccessException throw on bean read error
   * @throws InvocationTargetException throw on bean read error
   * @throws IntrospectionException throw on bean read error
   */
  private void applyPropertyValue(Object bean, List<String> propertyPath, Map<String,  String> propertyMapping) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
    try {
      String propertyName = propertyPath.remove(0);

      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, bean.getClass());
      Method readMethod = propertyDescriptor.getReadMethod();
      Method writeMethod = propertyDescriptor.getWriteMethod();
      
      if (propertyPath.isEmpty()) {
        String currentValue = (String) readMethod.invoke(bean);
        if (currentValue == null) {
          return;
        }
        
        String newValue = propertyMapping.get(currentValue);
        if (newValue != null) {
          writeMethod.invoke(bean, newValue); 
        }
      } else {
        Object value = readMethod.invoke(bean);

        if (value instanceof List<?>) {
          List<?> list = (List<?>) value;
          for (Object item : list) {
            applyPropertyValue(item, new ArrayList<>(propertyPath), propertyMapping);
          }
        } else {
          applyPropertyValue(value, new ArrayList<>(propertyPath), propertyMapping);
        }
      }
    } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
      logger.log(Level.WARNING, "Failed to apply PTV 7 compatibility changes", e);
      throw e;
    }
  }
  
  /**
   * Reads mappings from JSON file
   * 
   * @return mappings
   * @throws IOException throw on read error
   */
  private Mappings getMappings() throws IOException {
    return getObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("fi/metatavu/kuntaapi/server/rest/ptv7adapter/mappings.json"), Mappings.class);
  }

  /**
   * Creates an ObjectMapper
   * 
   * @return object mapper
   */
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

}
