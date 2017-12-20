package fi.otavanopisto.kuntaapi.server.index.search;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.index.AbstractIndexHander;
import fi.otavanopisto.kuntaapi.server.index.IndexReader;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportStopTimeSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class StopTimeSearcher {
  
  private static final String TYPE = "stoptime";
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
    SortOrder sortOrder = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
    
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

  private String resolveSortField(PublicTransportStopTimeSortBy sortBy) {
    if (sortBy != null) {
      switch (sortBy) {
        case DEPARTURE_TIME:
          return DEPARTURE_TIME_FIELD;
        case SCORE:
          return "_score";
        default:
          break;
      }
      
    }
    
    return null;
  }
  
  private SearchResult<PublicTransportStopTimeId> searchStopTimes(QueryBuilder queryBuilder, Long firstResult, Long maxResults, String sortField, SortOrder sortOrder) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search stop times. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    
    if (sortField != null) {
      requestBuilder.addSort(sortField, sortOrder);
    } else {
      requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, SortOrder.ASC);
    }
    
    return indexReader.search(requestBuilder, PublicTransportStopTimeId.class, "_id", ORGANIZATION_ID_FIELD);
  }

}
