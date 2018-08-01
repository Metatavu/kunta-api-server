package fi.metatavu.kuntaapi.server.rest;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import fi.metatavu.kuntaapi.server.controllers.HttpCacheController;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;

@ApplicationScoped
public class RestResponseBuilder {

  @Inject
  private HttpCacheController httpCacheController;
  
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
      responseBuilder = httpCacheController.modified(result, ids);
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
  
}
