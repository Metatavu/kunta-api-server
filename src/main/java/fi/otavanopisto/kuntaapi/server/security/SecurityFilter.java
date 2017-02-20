package fi.otavanopisto.kuntaapi.server.security;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
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
  
  @Context
  private HttpServletRequest request;
  
  @Override
  public void filter(ContainerRequestContext requestContext) {
    String authorizationHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
    if (StringUtils.isBlank(authorizationHeader)) {
      logUnuauthorizedRequest(requestContext, "Missing authorization header");
      return;
    }
    
    if (!StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME)) {
      logUnuauthorizedRequest(requestContext, "Invalid authorization scheme");
      return;
    }
    
    String authorization = decodeAuthorization(authorizationHeader);
    if (StringUtils.isBlank(authorization)) {
      logUnuauthorizedRequest(requestContext, "Invalid credentials");
      return;        
    }
    
    String[] credentials = StringUtils.split(authorization, ":", 2);
    if (credentials.length != 2) {
      logUnuauthorizedRequest(requestContext, "Missing credentials");
      return;        
    }
    
    Client client = clientController.findClientByClientIdAndSecret(credentials[0], credentials[1]);
    if (client == null) {
      logUnuauthorizedRequest(requestContext, "Invalid clientId or clientSecret");
      return;        
    }
    
    String method = StringUtils.upperCase(requestContext.getMethod());
    if (!isMethodAllowed(method, client)) {
      logUnuauthorizedRequest(requestContext, String.format("Client is not allowed to use %s", method));
    }

    clientContainer.setClient(client);
  }
  
  private boolean isMethodAllowed(String method, Client client) {
    if ("GET".equals(method)) {
      return true;
    }
    
    if ((client.getAccessType() == AccessType.READ_WRITE) || (client.getAccessType() == AccessType.UNRESTRICTED)) {
      return true;
    }
      
    return false;        
  }
  
  private String decodeAuthorization(String authorization) {
    try {
      return new String(Base64.decodeBase64(authorization.substring(AUTHENTICATION_SCHEME.length())), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.WARNING, "Invalid credential encoding", e);
      return null;
    }
  }

  private void logUnuauthorizedRequest(ContainerRequestContext requestContext, String message) {
    logger.log(Level.SEVERE, String.format("%s from %s", message, getRequestDetails(requestContext)));
  }

  private String getRequestDetails(ContainerRequestContext requestContext) {
    return String.format("%s (%s)", request.getRemoteHost(), requestContext.getHeaderString("User-Agent"));
  }
  
  
}
