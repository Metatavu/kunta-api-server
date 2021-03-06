package fi.metatavu.kuntaapi.server.index;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Singleton
public class IndexUpdater extends AbstractIndexHander {
  
  @Inject
  private Logger logger;
 
  @Override
  public void setup() {
    registerIndexable(IndexableOrganization.class);
    registerIndexable(IndexableService.class);
    registerIndexable(IndexablePage.class);
    registerIndexable(IndexableFile.class);
    registerIndexable(IndexableStopTime.class);
    registerIndexable(IndexableNewsArticle.class);
    registerIndexable(IndexableContact.class);
    registerIndexable(IndexableCode.class);
    registerIndexable(IndexableServiceLocationServiceChannel.class);
    registerIndexable(IndexableElectronicServiceChannel.class);
    registerIndexable(IndexablePhoneServiceChannel.class);
    registerIndexable(IndexablePrintableFormServiceChannel.class);
    registerIndexable(IndexableWebPageServiceChannel.class);
    registerIndexable(IndexableEmergency.class);
    registerIndexable(IndexableEnvironmentalWarning.class);
  }

  @Lock (LockType.READ)
  public void index(Indexable indexable) {
    if (!isEnabled()) {
      logger.warning("Could not index entity. Search functions are disabled");
      return;
    }
    
    getClient().prepareIndex(getIndex(), indexable.getType(), indexable.getId())
      .setSource(serialize(indexable))
      .execute()
      .actionGet();
  }
  
  @Lock (LockType.READ)
  public void remove(IndexRemove indexRemove) {
    if (!isEnabled()) {
      logger.warning("Could not remove entity. Search functions are disabled");
      return;
    }
    
    getClient()
      .prepareDelete(getIndex(), indexRemove.getType(), indexRemove.getId())
      .execute()
      .actionGet();
  }
  
  private void registerIndexable(Class<? extends Indexable> indexable) {
    Map<String, Map<String, Object>> properties = new HashMap<>();
    
    try {
      Indexable instance = indexable.newInstance();
      readProperties(indexable, properties);
      updateTypeMapping(instance.getType(), properties);
      
    } catch (IntrospectionException e) {
      logger.log(Level.SEVERE, String.format("Failed to inspect indexable %s", indexable.getName()), e);
    } catch (InstantiationException | IllegalAccessException e) {
      logger.log(Level.SEVERE, String.format("Failed to initialize indexable %s", indexable.getName()), e);
    }
  }

  @SuppressWarnings("unchecked")
  private void readProperties(Class<? extends Indexable> indexable, Map<String, Map<String, Object>> properties) throws InstantiationException, IllegalAccessException, IntrospectionException {
    BeanInfo beanInfo = Introspector.getBeanInfo(indexable);
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      readPropertyMapping(indexable, properties, propertyDescriptor);
    }
    
    Class<?> superclass = indexable.getSuperclass();
    if (superclass == null || superclass.equals(Object.class)) {
      return;
    }
    
    
    readProperties((Class<? extends Indexable>) indexable.getSuperclass(), properties);
  }

  private void readPropertyMapping(Class<? extends Indexable> indexable, Map<String, Map<String, Object>> properties, PropertyDescriptor propertyDescriptor) {
    String fieldName = propertyDescriptor.getName();
    Field propertyField = getField(indexable, fieldName);
    Method readMethod = propertyDescriptor.getReadMethod();
    
    if (propertyField != null || readMethod != null) {
      fi.metatavu.kuntaapi.server.index.Field fieldAnnotation = readMethod.getAnnotation(fi.metatavu.kuntaapi.server.index.Field.class);
      
      if (fieldAnnotation == null && propertyField != null) {
        fieldAnnotation = propertyField.getAnnotation(fi.metatavu.kuntaapi.server.index.Field.class);
      }
      
      if (fieldAnnotation != null) {
        Map<String, Object> fieldProperties = new HashMap<>();
        fieldProperties.put("type", fieldAnnotation.type());
        
        if (StringUtils.isNotBlank(fieldAnnotation.analyzer())) {
          fieldProperties.put("analyzer", fieldAnnotation.analyzer());
        }
        
        if (!"attachment".equals(fieldAnnotation.type())) {
          fieldProperties.put("index", fieldAnnotation.index());
          fieldProperties.put("store", fieldAnnotation.store());
        }
        
        properties.put(fieldName, fieldProperties);
      }
    }
  }

  private void updateTypeMapping(String type, Map<String, Map<String, Object>> properties) {
    if (!isEnabled()) {
      logger.warning("Could not update type mapping. Search functions are disabled");
      return;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Map<String, Map<String, Map<String, Object>>> mapping = new HashMap<>();
      mapping.put("properties", properties);
      String source = objectMapper.writeValueAsString(mapping);

      getClient()
        .admin()
        .indices()
        .preparePutMapping(getIndex())
        .setType(type)
        .setSource(source)
        .execute()
        .actionGet();
      
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize mapping update properties", e);
    }
  }
  
  @SuppressWarnings ("squid:S1166")
  private Field getField(Class<? extends Indexable> indexable, String fieldName) {
    try {
      return indexable.getDeclaredField(fieldName);
    } catch (NoSuchFieldException | SecurityException e) {
      return null;
    }
  }
}