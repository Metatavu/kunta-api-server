package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.PageSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class PageSearcher {
  
  private static final String TYPE = "page";
  private static final String PAGE_ID_FIELD = "pageId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<PageId> searchPages(String organizationId, String queryString, PageSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(queryStringQuery(queryString));
    
    return searchPages(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
  private SearchResult<PageId> searchPages(QueryBuilder queryBuilder, PageSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
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
    if (sortOrder == PageSortBy.SCORE) {
      requestBuilder
        .addSort("_score", order)
        .addSort(AbstractIndexHander.ORDER_INDEX_FIELD, order);
    } else {
      requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, order);
    }
      
    return indexReader.search(requestBuilder, PageId.class, PAGE_ID_FIELD, ORGANIZATION_ID_FIELD);
  }
   
}
