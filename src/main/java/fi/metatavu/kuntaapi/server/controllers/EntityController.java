package fi.metatavu.kuntaapi.server.controllers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
@SuppressWarnings ({"squid:S3306", "squid:S1948", "squid:UnusedPrivateMethod"})
public class EntityController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;

  public <T> List<T> sortEntitiesInNaturalOrder(List<T> entities) {
    return sortEntitiesInNaturalOrder(entities, SortDir.ASC);
  }
  
  public <T> List<T> sortEntitiesInNaturalOrder(List<T> entities, SortDir sortDir) {
    
    List<String> kuntaApiIds = new ArrayList<>(entities.size());
    for (Object entity : entities) {
      String id = getId(entity);
      if (id != null) {
        kuntaApiIds.add(id);
      }
    }
    
    Map<String, Long> orderIds = identifierController.getIdentifierOrderIndices(kuntaApiIds);
    entities.sort(new IdentifierComparator(orderIds, sortDir == null ? SortDir.ASC : sortDir));
    
    return entities;
  }
  
  private String getId(Object entity) {
    Method idGetter = getPropertyGetter(entity, "id");
    if (idGetter != null) {
      try {
        return (String) idGetter.invoke(entity);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        logger.log(Level.SEVERE, String.format("Failed to invoke id getter in class %s", entity.getClass().getName()), e);
      }
    }
    
    return null;
  }
  
  private Method getPropertyGetter(Object entity, String property) {
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());
      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        if (StringUtils.equals(property, propertyDescriptor.getName())) {
          return propertyDescriptor.getReadMethod();
        }
      }
    } catch (IntrospectionException e) {
      logger.log(Level.SEVERE, String.format("Failed to find getter for %s in class %s", property, entity.getClass().getName()), e);
    }
    
    return null;
  }
  private class IdentifierComparator implements Comparator<Object> {
    
    private SortDir sortDir;
    private Map<String, Long> orderIds;
    
    public IdentifierComparator(Map<String, Long> orderIds, SortDir sortDir) {
      this.orderIds = orderIds;
      this.sortDir = sortDir;
    }

    @Override
    public int compare(Object o1, Object o2) {
      String id1 = getId(o1);
      String id2 = getId(o2);
      
      Long orderNumber1 = orderIds.get(id1);
      Long orderNumber2 = orderIds.get(id2);
      
      if (orderNumber1 == null) {
        orderNumber1 = Long.MAX_VALUE;
      }

      if (orderNumber2 == null) {
        orderNumber2 = Long.MAX_VALUE;
      }
      
      if (sortDir == SortDir.DESC) {
        return orderNumber2.compareTo(orderNumber1);
      }
      
      return orderNumber1.compareTo(orderNumber2);
    }
  }
  
}
