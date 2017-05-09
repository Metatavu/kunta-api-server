package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Collections;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceSortOrder;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class ServiceSearcher {
  
  private static final String ORGANIZATION_IDS_FIELD = "organizationIds";
  private static final String SERVICE_ID_FIELD = "serviceId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<ServiceId> searchServices(OrganizationId organizationId, String text, ServiceSortOrder sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(queryStringQuery(text));
    
    if (organizationId != null) {
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.warning(String.format("Could not translate organization id %s into Kunta API id", organizationId));
        return new SearchResult<>(Collections.emptyList(), 0);
      }
      
      query.must(termQuery(ORGANIZATION_IDS_FIELD, kuntaApiOrganizationId.getId()));
    }
    
    return searchServices(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
  private SearchResult<ServiceId> searchServices(QueryBuilder queryBuilder, ServiceSortOrder sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder("service")
      .storedFields("serviceId")
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    
    SortOrder order = sortDir != null ? sortDir.toElasticSortOrder() : SortOrder.ASC;
    if (sortOrder == ServiceSortOrder.SCORE) {
      requestBuilder
        .addSort("_score", order)
        .addSort(AbstractIndexHander.ORDER_INDEX_FIELD, order);
    } else {
      requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, order);
    }
      
    return indexReader.search(requestBuilder, ServiceId.class, SERVICE_ID_FIELD);
  }
  
}
