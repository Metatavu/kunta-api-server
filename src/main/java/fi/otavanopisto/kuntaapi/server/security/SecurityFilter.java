package fi.otavanopisto.kuntaapi.server.security;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.ClientContainer;
import fi.otavanopisto.kuntaapi.server.controllers.SecurityController;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.Client;

@Provider
public class SecurityFilter implements ContainerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String AUTHENTICATION_SCHEME = "Basic ";

  @Inject
  private Logger logger; 
  
  @Inject
  private ClientContainer clientContainer;
  
  @Inject
  private SecurityController clientController;

  @Inject
  private RestSecurityWhitelist restSecurityWhitelist;
  
  @Context
  private HttpServletRequest request;
  
  @Override
  public void filter(ContainerRequestContext requestContext) {
    String path = requestContext.getUriInfo().getPath();
    if (restSecurityWhitelist.isWhitelisted(path)) {
      return;
    }
    
    String authorizationHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
    if (StringUtils.isBlank(authorizationHeader)) {
      handleUnuauthorizedRequest(requestContext, "Missing authorization header");
      return;
    }
    
    if (!StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME)) {
      handleUnuauthorizedRequest(requestContext, "Invalid authorization scheme");
      return;
    }
    
    String authorization = decodeAuthorization(authorizationHeader);
    if (StringUtils.isBlank(authorization)) {
      handleUnuauthorizedRequest(requestContext, "Invalid credentials");
      return;        
    }
    
    String[] credentials = StringUtils.split(authorization, ":", 2);
    if (credentials.length != 2) {
      handleUnuauthorizedRequest(requestContext, "Missing credentials");
      return;        
    }
    
    Client client = clientController.findClientByClientIdAndSecret(credentials[0], credentials[1]);
    if (client == null) {
      handleUnuauthorizedRequest(requestContext, "Invalid clientId or clientSecret");
      return;        
    }
    
    String method = StringUtils.upperCase(requestContext.getMethod());
    if (!isMethodAllowed(method, client)) {
      handleUnuauthorizedRequest(requestContext, String.format("Client is not allowed to use %s", method));
    }

    clientContainer.setClient(client);
  }
  
  /**
   * Returns whether method is allowed for the client
   * 
   * @param method method
   * @param client client
   * @return whether method is allowed for the client
   */
  private boolean isMethodAllowed(String method, Client client) {
    return ("GET".equals(method)) || (client.getAccessType() == AccessType.READ_WRITE) || (client.getAccessType() == AccessType.UNRESTRICTED);
  }
  
  /**
   * Decodes authorization string
   * 
   * @param authorization authorization string
   * @return decoded authorization string
   */
  private String decodeAuthorization(String authorization) {
    try {
      return new String(Base64.decodeBase64(authorization.substring(AUTHENTICATION_SCHEME.length())), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.WARNING, "Invalid credential encoding", e);
      return null;
    }
  }

  /**
   * Handles unauthorized request
   * 
   * @param requestContext request context
   * @param logMessage log message
   */
  private void handleUnuauthorizedRequest(ContainerRequestContext requestContext, String logMessage) {
    logger.log(Level.WARNING, () -> String.format("%s from %s", logMessage, getRequestDetails(requestContext)));
    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
  }

  /**
   * Returns details from the request
   * 
   * @param requestContext request context
   * @return details from the request
   */
  private String getRequestDetails(ContainerRequestContext requestContext) {
    return String.format("%s (%s)", request.getRemoteHost(), requestContext.getHeaderString("User-Agent"));
  }
  
}
