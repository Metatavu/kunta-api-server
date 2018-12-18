package fi.metatavu.kuntaapi.server.index.search;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.AbstractIndexHander;
import fi.metatavu.kuntaapi.server.index.AbstractIndexableServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexReader;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.persistence.dao.AbstractDAO;

public abstract class AbstractServiceChannelSearcher<T extends AbstractIndexableServiceChannel, I extends BaseId> {
  
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  private static final String SERVICE_CHANNEL_ID = "serviceChannelId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;
  
  /**
   * Searches Electronic Service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param queryString free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<I> searchServiceChannels(OrganizationId kuntaApiOrganizationId, String queryString, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery();

    if (kuntaApiOrganizationId != null) {
      query.must(matchQuery(ORGANIZATION_ID_FIELD, kuntaApiOrganizationId.getId()));
    }
    
    if (queryString != null) {
      query.must(queryStringQuery(queryString));
    }
    
    return searchElectronicServiceChannels(query, sortBy, sortDir, firstResult, maxResults);
  }
   
  @SuppressWarnings("unchecked")
  private SearchResult<I> searchElectronicServiceChannels(QueryBuilder queryBuilder, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not search service location service channels. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
        .requestBuilder(getType())
        .storedFields(SERVICE_CHANNEL_ID)
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
    
    return (SearchResult<I>) indexReader.search(requestBuilder, getIdClass(), SERVICE_CHANNEL_ID);
  }

  private String getType() {
    try {
      Class<? extends AbstractIndexableServiceChannel> genericTypeClass = getIndexableClass();
      if (genericTypeClass != null) {
        AbstractIndexableServiceChannel indexable = genericTypeClass.newInstance();
        if (indexable != null) {
          return indexable.getType();        
        }
      }
    } catch (InstantiationException | IllegalAccessException e) {
      logger.log(Level.SEVERE, "Failed to resolve type", e);
    }
    
    logger.log(Level.SEVERE, "Failed to resolve type");
    
    return null;
  }
  
  private Class<?> getTypeArgument(ParameterizedType parameterizedType, int index) {
    return (Class<?>) parameterizedType.getActualTypeArguments()[index];
  }

  @SuppressWarnings("unchecked")
  private Class<? extends T> getIndexableClass() {
    return (Class<? extends T>) getGenericClass(0);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends I> getIdClass() {
    return (Class<? extends I>) getGenericClass(1);
  }
  
  private Class<?> getGenericClass(int index) {
    Type genericSuperclass = getClass().getGenericSuperclass();

    if (genericSuperclass instanceof ParameterizedType) {
      return getTypeArgument((ParameterizedType) genericSuperclass, index);
    } else {
      if ((genericSuperclass instanceof Class<?>) && (AbstractDAO.class.isAssignableFrom((Class<?>) genericSuperclass))) {
        return getTypeArgument((ParameterizedType) ((Class<?>) genericSuperclass).getGenericSuperclass(), index);
      }
    }

    return null;
  }

}
