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

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ElectronicServiceChannelSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class ElectronicServiceChannelSearcher {
  
  private static final String TYPE = "electronic-service-channel";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String ELECTRONIC_SERVICE_CHANNEL_ID = "serviceChannelId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<ElectronicServiceChannelId> searchElectronicServiceChannels(OrganizationId kuntaApiOrganizationId, String queryString, ElectronicServiceChannelSortBy sortOrder, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery();

    if (kuntaApiOrganizationId != null) {
      query.must(matchQuery(ORGANIZATION_ID_FIELD, kuntaApiOrganizationId.getId()));
    }
    
    if (queryString != null) {
      query.must(queryStringQuery(queryString));
    }
    
    return searchElectronicServiceChannels(query, sortOrder, sortDir, firstResult, maxResults);
  }
   
  private SearchResult<ElectronicServiceChannelId> searchElectronicServiceChannels(QueryBuilder queryBuilder, ElectronicServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search service location service channels. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(TYPE)
        .storedFields(ELECTRONIC_SERVICE_CHANNEL_ID)
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
    
    return indexReader.search(requestBuilder, ElectronicServiceChannelId.class, ELECTRONIC_SERVICE_CHANNEL_ID);
  }

}
