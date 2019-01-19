package fi.metatavu.kuntaapi.server.index.search;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.util.Collection;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.index.AbstractIndexHander;
import fi.metatavu.kuntaapi.server.index.IndexReader;
import fi.metatavu.kuntaapi.server.index.IndexableEnvironmentalWarning;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.EnvironmentalWarningSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;

/**
 * Search controller for environmental warnings
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EnvironmentalWarningSearcher {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  /**
   * Search environmental warnings
   * 
   * @param organizationId organization id. Id source must be Kunta API
   * @param contexts filter by context. Optional
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<EnvironmentalWarningId> searchEnvironmentalWarning(String organizationId, Collection<String> contexts, EnvironmentalWarningSortBy sortBy, SortDir sortDir, 
      Integer firstResult, Integer maxResults) {
    
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(IndexableEnvironmentalWarning.ORGANIZATION_ID_FIELD, organizationId));

    if (contexts != null) {
      query.must(matchQuery(IndexableEnvironmentalWarning.CONTEXT_FIELD, contexts));
    }
    
    return searchEnvironmentalWarnings(query, sortBy, sortDir, firstResult, maxResults);
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
  private SearchResult<EnvironmentalWarningId> searchEnvironmentalWarnings(QueryBuilder queryBuilder, EnvironmentalWarningSortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(IndexableEnvironmentalWarning.TYPE)
      .storedFields(IndexableEnvironmentalWarning.ENVIRONMENTAL_WARNING_ID_FIELD, IndexableEnvironmentalWarning.ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    
    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
  
    switch (sortBy) {
      case START:
        requestBuilder.addSort(SortBuilders.fieldSort(IndexableEnvironmentalWarning.START_FIELD).order(revertOrder(order)));
      break;
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
    
    return indexReader.search(requestBuilder, EnvironmentalWarningId.class, IndexableEnvironmentalWarning.ENVIRONMENTAL_WARNING_ID_FIELD, IndexableEnvironmentalWarning.ORGANIZATION_ID_FIELD);
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
