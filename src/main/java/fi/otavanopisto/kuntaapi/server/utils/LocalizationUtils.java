package fi.otavanopisto.kuntaapi.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;

public class LocalizationUtils {
  
  private LocalizationUtils() {
  }
  
  @SafeVarargs
  public static List<String> getListsLanguages(final List<LocalizedValue>... lists) {
    List<String> result = new ArrayList<>();
    
    for (List<LocalizedValue> list : lists) {
      for (LocalizedValue item : list) {
        if (!result.contains(item.getLanguage())) {
          result.add(item.getLanguage());
        }
      }
    }
    
    return result;
  }

  @SafeVarargs
  public static List<String> getListsLanguages(String type, final List<LocalizedValue>... lists) {
    List<String> result = new ArrayList<>();
    
    for (List<LocalizedValue> list : lists) {
      for (LocalizedValue item : list) {
        if (StringUtils.equals(type, item.getType()) && (!result.contains(item.getLanguage()))) {
          result.add(item.getLanguage());
        }
      }
    }
    
    return result;
  }
  
  public static List<String> getLocaleValues(List<LocalizedValue> items, String language) {
    if (items == null) {
      return Collections.emptyList();
    }
    
    List<String> result = new ArrayList<>(items.size());
    
    for (LocalizedValue item : items) {
      if (StringUtils.equals(language, item.getLanguage())) {
        result.add(item.getValue());
      }
    }
    
    return result;
  }
  
  public static String getLocaleValue(List<LocalizedValue> items, String type, String language) {
    if (items == null) {
      return null;
    }
    
    for (LocalizedValue item : items) {
      if (((type == null) || (item.getType().equals(type))) && StringUtils.equals(language, item.getLanguage())) {
        return item.getValue();
      }
    }
    
    return null;
  }
  
  public static String getBestMatchingValue(String type, List<LocalizedValue> items, String language, String defaultLanguage) {
    if (items.isEmpty()) {
      return null;
    }
    
    Map<String, String> map = getLocalizedValueMap(type, items);
    if (map.isEmpty()) {
      return null;
    }
    
    if (map.containsKey(language)) {
      return map.get(language);
    }
    
    if (defaultLanguage != null && map.containsKey(defaultLanguage)) {
      return map.get(defaultLanguage);
    }
    
    return map.values().iterator().next();
  }
  
  /**
   * Returns typed localized items as map with language as key and value as value
   * 
   * @param type type
   * @param items localized items
   * @return localized items as map with language as key and value as value
   */
  public static Map<String, String> getLocalizedValueMap(String type, List<LocalizedValue> items) {
    Map<String, String> result = new HashMap<>();
    
    for (LocalizedValue item : items) {
      if (StringUtils.equals(type, item.getType())) {
        result.put(item.getLanguage(), item.getValue());
      }
    }
    
    return result;
  }
}
