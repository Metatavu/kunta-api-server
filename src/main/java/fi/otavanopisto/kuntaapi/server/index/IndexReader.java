package fi.otavanopisto.kuntaapi.server.index;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

@ApplicationScoped
@Singleton
@DependsOn (value = "IndexUpdater")
public class IndexReader extends AbstractIndexHander {
  
  public static final int MAX_RESULTS = 10000;
  
  @Override
  public void setup() {
    // No setup needed for reader
  }
 
  @Lock (LockType.READ)
  public SearchRequestBuilder requestBuilder(String... types) {
    return getClient()
      .prepareSearch(getIndex())
      .setTypes(types);
  }
  
  @Lock (LockType.READ)
  public SearchResponse executeSearch(SearchRequestBuilder searchRequest) {
    return searchRequest
      .execute()
      .actionGet(); 
  }

  @Lock (LockType.READ)
  public SearchHit[] search(SearchRequestBuilder searchRequest) {
    return executeSearch(searchRequest)
      .getHits()
      .getHits();
  }
  
}