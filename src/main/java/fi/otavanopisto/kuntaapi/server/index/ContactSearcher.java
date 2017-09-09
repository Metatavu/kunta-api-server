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
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.integrations.ContactSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class ContactSearcher {
  
  private static final String TYPE = "contact";
  private static final String CONTACT_ID_FIELD = "contactId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<ContactId> searchContacts(String organizationId, String queryString, ContactSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(queryStringQuery(queryString));
    
    return searchContacts(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
  private SearchResult<ContactId> searchContacts(QueryBuilder queryBuilder, ContactSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(CONTACT_ID_FIELD, ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    
    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;

    System.out.println("SORT BY: " + sortBy);
    System.out.println("SORT DIR: " + order);
    
    switch (sortBy) {
      case SCORE:
        requestBuilder.addSort(SortBuilders.scoreSort().order(order));
      break;
      case NATURAL:
      default:
        requestBuilder.addSort(SortBuilders.fieldSort(AbstractIndexHander.ORDER_INDEX_FIELD).order(order));
      break;
    }
      
    return indexReader.search(requestBuilder, ContactId.class, CONTACT_ID_FIELD, ORGANIZATION_ID_FIELD);
  }
   
}
