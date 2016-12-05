package fi.otavanopisto.kuntaapi.server.index;

import java.util.List;

public class SearchResult <T> {
  
  private List<T> result;
  
  public SearchResult(List<T> result) {
    this.result = result;
  }
  
  public List<T> getResult() {
    return result;
  }

}
