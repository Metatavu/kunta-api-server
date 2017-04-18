package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

@ApplicationScoped
public class ServiceLocationServiceChannelSearcher {
  
  private static final String TYPE = "service-location-service-channel";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<ServiceLocationServiceChannelId> searchServiceLocationServiceChannels(String queryString, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(queryStringQuery(queryString));
    
    return searchServiceLocationServiceChannels(query, firstResult, maxResults);
  }
   
  private SearchResult<ServiceLocationServiceChannelId> searchServiceLocationServiceChannels(QueryBuilder queryBuilder, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search service location service channels. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(TYPE)
        .storedFields(ORGANIZATION_ID_FIELD)
        .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, SortOrder.ASC);
      
    return new SearchResult<>(getServiceLocationServiceChannelIds(indexReader.search(requestBuilder)));
  }
  
  private List<ServiceLocationServiceChannelId> getServiceLocationServiceChannelIds(SearchHit[] hits) {
    List<ServiceLocationServiceChannelId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField searchHitField = fields.get(ORGANIZATION_ID_FIELD);
      String serviceLocationServiceChannelId = searchHitField.getValue();
      if (StringUtils.isNotBlank(serviceLocationServiceChannelId)) {
        result.add(kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationServiceChannelId));
      }
    }
    
    return result;
  }

}
