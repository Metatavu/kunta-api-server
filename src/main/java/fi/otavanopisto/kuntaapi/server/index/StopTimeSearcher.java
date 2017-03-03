package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportStopTimeSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class StopTimeSearcher {
  
  private static final String TYPE = "stoptime";
  private static final String ID_FIELD = "id";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String TRIP_ID_FIELD = "tripId";
  private static final String STOP_ID_FIELD = "stopId";
  private static final String DEPARTURE_TIME_FIELD = "departureTime";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  @SuppressWarnings ("squid:S00107")
  public SearchResult<PublicTransportStopTimeId> searchStopTimes(String organizationId, String tripId, String stopId, Integer depratureTimeOnOrAfter, PublicTransportStopTimeSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = createQuery(organizationId, tripId, stopId, depratureTimeOnOrAfter);
    String sortField = resolveSortField(sortBy);
    SortOrder sortOrder = resolveSortOrder(sortDir);
    
    return searchStopTimes(query, firstResult, maxResults, sortField, sortOrder);
  }

  private BoolQueryBuilder createQuery(String organizationId, String tripId, String stopId, Integer depratureTimeOnOrAfter) {
    BoolQueryBuilder query = boolQuery();
    
    if (organizationId != null) {
      query.must(termQuery(ORGANIZATION_ID_FIELD, organizationId));      
    }
    
    if (tripId != null) {
      query.must(termQuery(TRIP_ID_FIELD, tripId));      
    }
    
    if (stopId != null) {
      query.must(termQuery(STOP_ID_FIELD, stopId));      
    }
    
    if (depratureTimeOnOrAfter != null) {
      query.must(rangeQuery(DEPARTURE_TIME_FIELD).gte(depratureTimeOnOrAfter));
    }
    return query;
  }

  private SortOrder resolveSortOrder(SortDir sortDir) {
    return sortDir == SortDir.ASC ? SortOrder.ASC : SortOrder.DESC;
  }

  private String resolveSortField(PublicTransportStopTimeSortBy sortBy) {
    String sortField = null;
    if (sortBy != null && sortBy == PublicTransportStopTimeSortBy.DEPARTURE_TIME) {
      sortField = DEPARTURE_TIME_FIELD;
    }
    return sortField;
  }
  
  private SearchResult<PublicTransportStopTimeId> searchStopTimes(QueryBuilder queryBuilder, Long firstResult, Long maxResults, String sortField, SortOrder sortOrder) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search stop times. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(TYPE)
        .storedFields(ORGANIZATION_ID_FIELD, ID_FIELD)
        .setQuery(queryBuilder);
    
    if (firstResult != null) {
      requestBuilder.setFrom(firstResult.intValue());
    }
    
    if (maxResults != null) {
      requestBuilder.setSize(maxResults.intValue());
    }
    
    if (sortField != null) {
      requestBuilder.addSort(sortField, sortOrder);
    }
      
    return new SearchResult<>(getStopTimeIds(indexReader.search(requestBuilder)));
  }
  
  private List<PublicTransportStopTimeId> getStopTimeIds(SearchHit[] hits) {
    List<PublicTransportStopTimeId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      
      SearchHitField idField = fields.get(ID_FIELD);
      String stopTimeId = idField.getValue();
      
      SearchHitField organizationIdField = fields.get(ORGANIZATION_ID_FIELD);
      String organizationId = organizationIdField.getValue();
      
      if (StringUtils.isNotBlank(organizationId)) {
        result.add(new PublicTransportStopTimeId(new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationId), KuntaApiConsts.IDENTIFIER_NAME, stopTimeId));
      }
    }
    
    return result;
  }

}
