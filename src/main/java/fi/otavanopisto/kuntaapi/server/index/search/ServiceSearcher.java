package fi.otavanopisto.kuntaapi.server.index.search;

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
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.AbstractIndexHander;
import fi.otavanopisto.kuntaapi.server.index.IndexReader;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class ServiceSearcher {
  
  private static final String ORGANIZATION_IDS_FIELD = "organizationIds";
  private static final String ELECTRONIC_SERVICE_CHANNEL_IDS_FIELD = "electronicServiceChannelIds";
  private static final String PHONE_SERVICE_CHANNEL_IDS_FIELD = "phoneServiceChannelIds";
  private static final String SERVICE_LOCATION_SERVICE_CHANNEL_IDS_FIELD = "serviceLocationServiceChannelIds";
  private static final String PRINTABLE_FORM_SERVICE_CHANNEL_IDS_FIELD = "printableFormServiceChannelIds";
  private static final String WEB_PAGE_SERVICE_CHANNEL_IDS_FIELD = "webPageServiceChannelIds";

  private static final String SERVICE_ID_FIELD = "serviceId";
  private static final int DEFAULT_MAX_RESULTS = 50;
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IndexReader indexReader;

  @SuppressWarnings ("squid:S00107")
  public SearchResult<ServiceId> searchServices(OrganizationId organizationId, ElectronicServiceChannelId electronicServiceChannelId, PhoneServiceChannelId phoneServiceChannelId, PrintableFormServiceChannelId printableFormServiceChannelId, ServiceLocationServiceChannelId serviceLocationServiceChannelId, WebPageServiceChannelId webPageServiceChannelId, String text, ServiceSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery();
    
    if (text != null) {
      query.must(queryStringQuery(text));
    }
    
    if (organizationId != null) {
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.warning(() -> String.format("Could not translate organization id %s into Kunta API id", organizationId));
        return new SearchResult<>(Collections.emptyList(), 0);
      }
      
      query.must(termQuery(ORGANIZATION_IDS_FIELD, kuntaApiOrganizationId.getId()));
    }
    
    if (electronicServiceChannelId != null) {
      query.must(termQuery(ELECTRONIC_SERVICE_CHANNEL_IDS_FIELD, electronicServiceChannelId.getId()));        
    }

    if (phoneServiceChannelId != null) {
      query.must(termQuery(PHONE_SERVICE_CHANNEL_IDS_FIELD, phoneServiceChannelId.getId()));        
    }

    if (printableFormServiceChannelId != null) {
      query.must(termQuery(PRINTABLE_FORM_SERVICE_CHANNEL_IDS_FIELD, printableFormServiceChannelId.getId()));        
    }

    if (serviceLocationServiceChannelId != null) {
      query.must(termQuery(SERVICE_LOCATION_SERVICE_CHANNEL_IDS_FIELD, serviceLocationServiceChannelId.getId()));        
    }

    if (webPageServiceChannelId != null) {
      query.must(termQuery(WEB_PAGE_SERVICE_CHANNEL_IDS_FIELD, webPageServiceChannelId.getId()));        
    }
    
    return searchServices(query, sortOrder, sortDir, firstResult, maxResults);
  }
  
  private SearchResult<ServiceId> searchServices(QueryBuilder queryBuilder, ServiceSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder("service")
      .storedFields(SERVICE_ID_FIELD)
      .setQuery(queryBuilder);
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : DEFAULT_MAX_RESULTS);
    
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
    
    return indexReader.search(requestBuilder, ServiceId.class, SERVICE_ID_FIELD);
  }
  
}
