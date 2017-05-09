package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

@ApplicationScoped
public class OrganizationSearcher {
  
  private static final String TYPE = "organization";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String BUSINESS_NAME_UT_FIELD = "businessNameUT";
  private static final String BUSINESS_CODE_FIELD = "businessCode";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<OrganizationId> searchOrganizations(String queryString, String businessCode, String businessName, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery().
      must(queryStringQuery(queryString));
    
    if (businessCode != null) {
      query.must(termQuery(BUSINESS_CODE_FIELD, businessCode));
    }
    
    if (businessName != null) {
      query.must(termQuery(BUSINESS_NAME_UT_FIELD, businessName));
    }
    
    return searchOrganizations(query, firstResult, maxResults);
  }
   
  public SearchResult<OrganizationId> searchOrganizationsByBusinessCode(String businessCode, Long firstResult, Long maxResults) {
    return searchOrganizations(termQuery(BUSINESS_CODE_FIELD, businessCode), firstResult, maxResults);
  }
  
  public SearchResult<OrganizationId> searchOrganizationsByBusinessName(String businessName, Long firstResult, Long maxResults) {
    return searchOrganizations(termQuery(BUSINESS_NAME_UT_FIELD, businessName), firstResult, maxResults);
  }
  
  public SearchResult<OrganizationId> searchOrganizationsByBusinessCodeAndBusinessName(String businessCode, String businessName, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(termQuery(BUSINESS_CODE_FIELD, businessCode))
      .must(termQuery(BUSINESS_NAME_UT_FIELD, businessName));
    
    return searchOrganizations(query, firstResult, maxResults);
  }
  
  private SearchResult<OrganizationId> searchOrganizations(QueryBuilder queryBuilder, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search organizations. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(TYPE)
        .storedFields(ORGANIZATION_ID_FIELD)
        .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, SortOrder.ASC);
    
    return indexReader.search(requestBuilder, OrganizationId.class, ORGANIZATION_ID_FIELD);
  }

}
