package fi.otavanopisto.kuntaapi.server.index;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.BaseId;

public class SearchResult <T extends BaseId> {
  
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
  
}
