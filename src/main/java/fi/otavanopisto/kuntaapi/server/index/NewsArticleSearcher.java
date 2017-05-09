package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
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

import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.NewsSortOrder;
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
  
  public SearchResult<NewsArticleId> searchNewsArticlesByTag(String organizationId, String tag, NewsSortOrder sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(matchQuery(TAGS_FIELD, tag));
    
    return searchNewsArticles(query, sortOrder, sortDir, firstResult, maxResults);
  }

  public SearchResult<NewsArticleId> searchNewsArticlesByFreeText(String organizationId, String search, NewsSortOrder sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(queryStringQuery(search));
    
    return searchNewsArticles(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
  private SearchResult<NewsArticleId> searchNewsArticles(QueryBuilder queryBuilder, NewsSortOrder sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
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
    
    if (sortOrder == NewsSortOrder.SCORE) {
      requestBuilder.addSort("_score", sortDir.toElasticSortOrder());
    } else {
      requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, sortDir.toElasticSortOrder());
    }
    
    return new SearchResult<>(getNewsArticleIds(indexReader.search(requestBuilder)));
  }
  
  private List<NewsArticleId> getNewsArticleIds(SearchHit[] hits) {
    List<NewsArticleId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField newsArticleHitField = fields.get(MEWS_ARTICLE_ID_FIELD);
      SearchHitField organizationHitField = fields.get(ORGANIZATION_ID_FIELD);
      
      String newsArticleId = newsArticleHitField.getValue();
      
      if (StringUtils.isNotBlank(newsArticleId)) {
        OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationHitField.getValue());
        result.add(new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newsArticleId));
      }
    }
    
    return result;
  }
   
}
