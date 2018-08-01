package fi.metatavu.kuntaapi.server.utils;

import java.util.List;

public class ListUtils {
  
  private ListUtils() {
    
  }

  public static <T> List<T> limit(List<T> list, Long firstResult, Long maxResults) {
    return limit(list, firstResult != null ? firstResult.intValue() : null, maxResults != null ? maxResults.intValue() : null);
  }
  
  public static <T> List<T> limit(List<T> list, Integer firstResult, Integer maxResults) {
    int resultCount = list.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    return list.subList(firstIndex, toIndex);
  }
  
}
