package fi.metatavu.kuntaapi.server.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import fi.metatavu.kuntaapi.server.controllers.HttpCacheController;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.rest.ptv7adapter.Ptv7Adapter;

@ApplicationScoped
public class RestResponseBuilder {

  @Inject
  private Logger logger;

  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private Ptv7Adapter ptv7Adapter;

  public Response getNotModified(Request request, BaseId baseId) {
    return httpCacheController.getNotModified(request, baseId);
  }
  
  public ResponseBuilder notModified(Request request, List<String> ids) {
    return httpCacheController.notModified(request, ids); 
  }

  public Response sendModified(Object entity, String id) {
    return httpCacheController.sendModified(applyEntityTranslations(entity), id);
  }
  
  public <T> Response buildResponse(SearchResult<T> searchResult, Request request) {
    if (searchResult == null) {
      return buildResponse(Collections.emptyList(), 0l, request);
    } else {
      return buildResponse(searchResult.getResult(), searchResult.getTotalHits(), request);
    }    
  }

  public <T> Response buildResponse(List<T> result, Long totalHits, Request request) {
    List<String> ids = httpCacheController.getEntityIds(result);
    ResponseBuilder responseBuilder = httpCacheController.notModified(request, ids);
    if (responseBuilder == null) {
      responseBuilder = httpCacheController.modified(applyEntityListTranslations(result), ids);
    }
    
    if (totalHits != null) {
      responseBuilder.header("X-Kunta-API-Total-Results", totalHits);
    }
      
    return responseBuilder.build();
  }
  
  public <T> Response buildErrorResponse(IntegrationResponse<T> integrationResponse) {
    return Response.status(integrationResponse.getStatus())
      .entity(integrationResponse.getMessage())
      .build();
  }
  
  /**
   * Return current HttpServletRequest
   * 
   * @return current http servlet request
   */
  private HttpServletRequest getHttpServletRequest() {
    return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
  }  

  /**
   * Applies translations to entity if needed
   * 
   * @param entity entity
   * @return entity with required translations applied
   */
  @SuppressWarnings("unchecked")
  private <T> T applyEntityTranslations(T entity) {
    boolean ptv7Compatibility = !"false".equalsIgnoreCase(getHttpServletRequest().getHeader("Kunta-API-PTV7-Compatibility"));
    
    if (ptv7Compatibility) {
      try {
        return (T) ptv7Adapter.translate(entity);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to apply PTV 7 naming convensions", e);
      }
    }

    return entity;
  }

  /**
   * Applies translations to entities if needed
   * 
   * @param entities entities
   * @return entities with required translations applied
   */
  private <T> List<T> applyEntityListTranslations(List<T> entities) {
    return entities.stream()
      .map(this::applyEntityTranslations)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
}
