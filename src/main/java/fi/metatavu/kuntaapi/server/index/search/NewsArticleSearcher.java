package fi.metatavu.kuntaapi.server.index.search;

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

import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.index.AbstractIndexHander;
import fi.metatavu.kuntaapi.server.index.IndexReader;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.NewsSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class NewsArticleSearcher {
  
  private static final String TYPE = "newsarticle";
  private static final String MEWS_ARTICLE_ID_FIELD = "newsArticleId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String TAGS_FIELD = "tags";
  private static final String SLUG_FIELD = "slug";
  private static final String PUBLISHED_FIELD = "published";
  private static final String ORDER_NUMBER_FIELD = "orderNumber";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  /**
   * Search news articles. 
   * 
   * @param organizationId organixation id. Id source must be Kunta API
   * @param search free text search. Optional
   * @param tag filter by tag. Optional
   * @param slug filter by slug. Optional
   * @param publishedBefore filter by published before. Optional
   * @param publishedAfter filter by published after. Optional
   * @param sortOrder sort order
   * @param sortDir sort direction
   * @param firstResult first result index
   * @param maxResults max results
   * @return result
   */
  @SuppressWarnings ("squid:S00107")
  public SearchResult<NewsArticleId> searchNewsArticles(String organizationId, String search, String tag, String slug, 
      OffsetDateTime publishedBefore, OffsetDateTime publishedAfter, NewsSortBy sortOrder, SortDir sortDir, 
      Long firstResult, Long maxResults) {
    
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId));

    if (tag != null) {
      query.must(matchQuery(TAGS_FIELD, tag));
    }

    if (slug != null) {
      query.must(matchQuery(SLUG_FIELD, slug));
    }
    
    if (search != null) {
      query.must(queryStringQuery(search));
    }
    
    if (publishedBefore != null || publishedAfter != null) {
      RangeQueryBuilder rangeQuery = rangeQuery(PUBLISHED_FIELD);
      
      if (publishedAfter != null) {
        rangeQuery.gte(publishedAfter.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
      }
      
      if (publishedBefore != null) {
        rangeQuery.lte(publishedBefore.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
      }    
      
      query.must(rangeQuery);
    }
    
    return searchNewsArticles(query, sortOrder, sortDir, firstResult, maxResults);
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
  private SearchResult<NewsArticleId> searchNewsArticles(QueryBuilder queryBuilder, NewsSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    

    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(MEWS_ARTICLE_ID_FIELD, ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);

    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
    switch (sortBy) {
      case SCORE:
        requestBuilder.addSort(SortBuilders.scoreSort().order(order));
      break;
      case ORDER_NUMBER_PUBLISHED:
        requestBuilder.addSort(SortBuilders.fieldSort(ORDER_NUMBER_FIELD).order(order))
                      .addSort(SortBuilders.fieldSort(PUBLISHED_FIELD).order(revertOrder(order)));
      break;
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
    
    return indexReader.search(requestBuilder, NewsArticleId.class, MEWS_ARTICLE_ID_FIELD, ORGANIZATION_ID_FIELD);
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
