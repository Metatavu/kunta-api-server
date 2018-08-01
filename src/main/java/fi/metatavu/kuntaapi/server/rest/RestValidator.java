package fi.metatavu.kuntaapi.server.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.metatavu.kuntaapi.server.rest.model.BadRequest;

@ApplicationScoped
public class RestValidator {

  private static final String MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER = "maxResults must by a positive integer";
  private static final String FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER = "firstResult must by a positive integer";

  public Response validateListLimitParams(Long firstResult, Long maxResults) {
    if (firstResult != null && firstResult < 0) {
      return createBadRequest(FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    if (maxResults != null && maxResults < 0) {
      return createBadRequest(MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    return null;
  }

  private Response createBadRequest(String message) {
    BadRequest badRequest = new BadRequest();
    badRequest.setMessage(message);
    badRequest.setCode(Status.BAD_REQUEST.getStatusCode());
    return Response.status(Status.BAD_REQUEST).entity(badRequest).build();
  }
  
}
