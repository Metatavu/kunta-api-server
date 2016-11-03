package fi.otavanopisto.kuntaapi.server.index;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

@ApplicationScoped
@Singleton
public class IndexReader extends AbstractIndexHander {
 
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