package fi.otavanopisto.kuntaapi.server.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.restfulptv.client.model.LocalizedListItem;

public class LocalizationUtils {
  
  private LocalizationUtils() {
  }
  
  @SafeVarargs
  public static List<String> getListsLanguages(final List<LocalizedListItem>... lists) {
    List<String> result = new ArrayList<>();
    
    for (List<LocalizedListItem> list : lists) {
      for (LocalizedListItem item : list) {
        if (!result.contains(item.getLanguage())) {
          result.add(item.getLanguage());
        }
      }
    }
    
    return result;
  }

  @SafeVarargs
  public static List<String> getListsLanguages(String type, final List<LocalizedListItem>... lists) {
    List<String> result = new ArrayList<>();
    
    for (List<LocalizedListItem> list : lists) {
      for (LocalizedListItem item : list) {
        if (StringUtils.equals(type, item.getType()) && (!result.contains(item.getLanguage()))) {
          result.add(item.getLanguage());
        }
      }
    }
    
    return result;
  }
  
  public static String getBestMatchingValue(String type, List<LocalizedListItem> items, String language, String defaultLanguage) {
    if (items.isEmpty()) {
      return null;
    }
    
    Map<String, String> map = mapLocalizedListItems(type, items);
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
  
  private static Map<String, String> mapLocalizedListItems(String type, List<LocalizedListItem> items) {
    Map<String, String> result = new HashMap<>();
    
    for (LocalizedListItem item : items) {
      if (StringUtils.equals(type, item.getType())) {
        result.put(item.getLanguage(), item.getValue());
      }
    }
    
    return result;
  }
}
