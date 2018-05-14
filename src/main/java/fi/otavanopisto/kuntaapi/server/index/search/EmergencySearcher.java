package fi.otavanopisto.kuntaapi.server.index.search;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.index.AbstractIndexHander;
import fi.otavanopisto.kuntaapi.server.index.IndexReader;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencySortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class EmergencySearcher {
  
  private static final String TYPE = "emergency";
  private static final String EMERGENCY_ID_FIELD = "emergencyId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String TIME_FIELD = "time";
  private static final String LOCATION_FIELD = "location";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  /**
   * Search emergencies. 
   * 
   * @param organizationId organixation id. Id source must be Kunta API
   * @param search free text search. Optional
   * @param location emergency location
   * @param before filter by time before. Optional
   * @param after filter by time after. Optional
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result index
   * @param maxResults max results
   * @return result
   */
  @SuppressWarnings ("squid:S00107")
  public SearchResult<EmergencyId> searchEmergencys(String organizationId, String search, String location, 
      OffsetDateTime before, OffsetDateTime after, EmergencySortBy sortBy, SortDir sortDir, 
      Integer firstResult, Integer maxResults) {
    
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId));

    if (location != null) {
      query.must(matchQuery(LOCATION_FIELD, location));
    }
    
    if (search != null) {
      query.must(queryStringQuery(search));
    }
    
    if (before != null || after != null) {
      RangeQueryBuilder rangeQuery = rangeQuery(TIME_FIELD);
      
      if (after != null) {
        rangeQuery.gte(after.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
      }
      
      if (before != null) {
        rangeQuery.lte(before.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
      }    
      
      query.must(rangeQuery);
    }
    
    return searchEmergencys(query, sortBy, sortDir, firstResult, maxResults);
  }
  
  /**
   * Executes query and applies sorts and limits
   * 
   * @param queryBuilder query
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  private SearchResult<EmergencyId> searchEmergencys(QueryBuilder queryBuilder, EmergencySortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }

    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(EMERGENCY_ID_FIELD, ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);

    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
    switch (sortBy) {
      case START:
        requestBuilder.addSort(SortBuilders.fieldSort(TIME_FIELD).order(revertOrder(order)));
      break;
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
    
    return indexReader.search(requestBuilder, EmergencyId.class, EMERGENCY_ID_FIELD, ORGANIZATION_ID_FIELD);
  }

  /**
   * Reverts sort order
   * 
   * @param order original order
   * @return reverted order
   */
  private SortOrder revertOrder(SortOrder order) {
    if (order == null) {
      return null;
    }
    
    if (order == SortOrder.ASC) {
      return SortOrder.DESC;
    }
    
    return SortOrder.ASC;
  }
   
}
