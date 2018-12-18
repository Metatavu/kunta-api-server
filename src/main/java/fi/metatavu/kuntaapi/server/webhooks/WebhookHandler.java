package fi.metatavu.kuntaapi.server.webhooks;

import javax.servlet.http.HttpServletRequest;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public interface WebhookHandler {

  public String getType();
  
  public boolean handle(OrganizationId organizationId, HttpServletRequest request);
  
}
