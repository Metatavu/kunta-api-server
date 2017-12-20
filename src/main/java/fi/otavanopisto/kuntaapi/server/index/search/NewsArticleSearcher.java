package fi.otavanopisto.kuntaapi.server.index.search;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.index.AbstractIndexHander;
import fi.otavanopisto.kuntaapi.server.index.IndexReader;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.NewsSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class NewsArticleSearcher {
  
  private static final String TYPE = "newsarticle";
  private static final String MEWS_ARTICLE_ID_FIELD = "newsArticleId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String TAGS_FIELD = "tags";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  public SearchResult<NewsArticleId> searchNewsArticlesByTag(String organizationId, String tag, NewsSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(matchQuery(TAGS_FIELD, tag));
    
    return searchNewsArticles(query, sortOrder, sortDir, firstResult, maxResults);
  }

  public SearchResult<NewsArticleId> searchNewsArticlesByFreeText(String organizationId, String search, NewsSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(queryStringQuery(search));
    
    return searchNewsArticles(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
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
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
    
    return indexReader.search(requestBuilder, NewsArticleId.class, MEWS_ARTICLE_ID_FIELD, ORGANIZATION_ID_FIELD);
  }
   
}
