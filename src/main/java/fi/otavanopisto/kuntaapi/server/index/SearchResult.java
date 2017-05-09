package fi.otavanopisto.kuntaapi.server.index;

import java.util.Collections;
import java.util.List;

public class SearchResult <T> {
  
  private List<T> result;
  private long totalHits;
  
  public SearchResult(List<T> result, long totalHits) {
    this.result = result;
    this.totalHits = totalHits;
  }
  
  public List<T> getResult() {
    return result;
  }
  
  public long getTotalHits() {
    return totalHits;
  }
  
  public static <T> SearchResult<T> emptyResult() {
    return new SearchResult<>(Collections.emptyList(), 0);
  }
  
}
