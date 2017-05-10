package fi.otavanopisto.kuntaapi.server.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

@ApplicationScoped
@Singleton
@DependsOn (value = "IndexUpdater")
public class IndexReader extends AbstractIndexHander {
  
  public static final int MAX_RESULTS = 10000;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
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
  public <T extends BaseId> SearchResult<T> search(SearchRequestBuilder searchRequest, Class<T> idClass, String idField) {
    return search(searchRequest, idClass, idField, null); 
  }
  
  @Lock (LockType.READ)
  public <T extends BaseId> SearchResult<T> search(SearchRequestBuilder searchRequest, Class<T> idClass, String idField, String organizationField) {
    SearchHits searchHits = executeSearch(searchRequest).getHits();
    return fromHits(searchHits, idClass, idField, organizationField); 
  }
  
  private SearchResponse executeSearch(SearchRequestBuilder searchRequest) {
    return searchRequest
      .execute()
      .actionGet(); 
  }
  
  private <T extends BaseId> SearchResult<T> fromHits(SearchHits searchHits, Class<T> idClass, String idField, String organizationField) {
    SearchHit[] hits = searchHits.getHits();

    List<T> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      OrganizationId organizationId = null;
      
      if (organizationField != null) {
        SearchHitField organizationHitField = fields.get(organizationField);
        organizationId = kuntaApiIdFactory.createOrganizationId(organizationHitField.getValue());
      }
      
      String id = "_id".equals(idField) ? hit.getId() : fields.get(idField).getValue();
      
      if (StringUtils.isNotBlank(id)) {
        T kuntaApiId = kuntaApiIdFactory.createId(idClass, organizationId, id);
        if (!result.contains(kuntaApiId)) {
          result.add(kuntaApiId);
        }
      }
    }
    
    return new SearchResult<>(result, searchHits.getTotalHits());
  }

  
}