package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.PageSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class PageSearcher {
  
  private static final String TYPE = "page";
  private static final String PAGE_ID_FIELD = "pageId";
  private static final String PARENT_ID_FIELD = "parentId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String MENU_ORDER_FIELD = "menuOrder";
  private static final String TITLE_RAW_FIELD = "titleRaw";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  /**
   * Search pages. 
   * 
   * @param organizationId organixation id. Id source must be Kunta API
   * @param queryString free text search. Optional
   * @param sortOrder sort order
   * @param sortDir sort direction
   * @param onlyRootPages returns only root pages. Should not be used with parentId parameter
   * @param parentId returns only pages with specified parent id. Id source must be Kunta API. Should not be used with onlyRootPages parameter 
   * @param firstResult first result index
   * @param maxResults max results
   * @return result
   */
  @SuppressWarnings ("squid:S00107")
  public SearchResult<PageId> searchPages(String organizationId, String queryString, PageSortBy sortOrder, SortDir sortDir, boolean onlyRootPages, PageId parentId, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId));
    
    if (queryString != null) {
      query.must(queryStringQuery(queryString));
    }
    
    if (parentId != null) {
      if (!KuntaApiConsts.IDENTIFIER_NAME.equals(parentId.getSource())) {
        if (logger.isLoggable(Level.WARNING)) {
          logger.warning(String.format("Could not execute search. Parent id source was unsupported (%s)", parentId.getSource()));
        }
        
        return null;
      }
      
      query.must(matchQuery(PARENT_ID_FIELD, parentId.getId()));
    }
    
    if (onlyRootPages) {
      query.mustNot(existsQuery(PARENT_ID_FIELD));
    }
    
    return searchPages(query, sortOrder, sortDir, firstResult, maxResults);
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
  private SearchResult<PageId> searchPages(QueryBuilder queryBuilder, PageSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(PAGE_ID_FIELD, ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    
    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
    switch (sortBy) {
      case SCORE:
        requestBuilder.addSort(SortBuilders.scoreSort().order(order));
      break;
      case MENU:
        requestBuilder.addSort(SortBuilders.fieldSort(MENU_ORDER_FIELD).order(order))
                      .addSort(SortBuilders.fieldSort(TITLE_RAW_FIELD).order(order));
      break;
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
      
    return indexReader.search(requestBuilder, PageId.class, PAGE_ID_FIELD, ORGANIZATION_ID_FIELD);
  }
   
}
