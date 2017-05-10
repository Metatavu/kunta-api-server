package fi.otavanopisto.kuntaapi.server.rest;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;

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
  
}
