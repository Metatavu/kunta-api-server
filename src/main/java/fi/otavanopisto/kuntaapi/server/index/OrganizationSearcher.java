package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class OrganizationSearcher extends AbstractSearcher {
  
  private static final String TYPE = "organization";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String BUSINESS_NAME_UT_FIELD = "businessNameUT";
  private static final String BUSINESS_CODE_FIELD = "businessCode";
  
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
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(TYPE)
        .storedFields(ORGANIZATION_ID_FIELD)
        .setQuery(queryBuilder);
    
    if (firstResult != null) {
      requestBuilder.setFrom(firstResult.intValue());
    }
    
    if (maxResults != null) {
      requestBuilder.setSize(maxResults.intValue());
    }
      
    return new SearchResult<>(getOrganizationIds(indexReader.search(requestBuilder)));
  }
  
  private List<OrganizationId> getOrganizationIds(SearchHit[] hits) {
    List<OrganizationId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField searchHitField = fields.get(ORGANIZATION_ID_FIELD);
      String organizationId = searchHitField.getValue();
      if (StringUtils.isNotBlank(organizationId)) {
        result.add(new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationId));
      }
    }
    
    return result;
  }

}
