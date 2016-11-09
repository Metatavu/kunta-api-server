package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class ServiceSearcher extends AbstractSearcher {
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<ServiceId> searchServices(String text, Long firstResult, Long maxResults) {
    QueryStringQueryBuilder query = queryStringQuery(text);
    return searchServices(query, firstResult, maxResults);
  }
  
  private SearchResult<ServiceId> searchServices(QueryBuilder queryBuilder, Long firstResult, Long maxResults) {
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder("service")
      .storedFields("serviceId")
      .setQuery(queryBuilder);
    
    if (firstResult != null) {
      requestBuilder.setFrom(firstResult.intValue());
    }
    
    if (maxResults != null) {
      requestBuilder.setSize(maxResults.intValue());
    }
      
    return new SearchResult<>(getServiceIds(indexReader.search(requestBuilder)));
  }
  
  private List<ServiceId> getServiceIds(SearchHit[] hits) {
    List<ServiceId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField searchHitField = fields.get("serviceId");
      String serviceId = searchHitField.getValue();
      if (StringUtils.isNotBlank(serviceId)) {
        result.add(new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, serviceId));
      }
    }
    
    return result;
  }
   
}
