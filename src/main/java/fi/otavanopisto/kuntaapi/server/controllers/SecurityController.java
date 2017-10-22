package fi.otavanopisto.kuntaapi.server.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.persistence.dao.ClientDAO;
import fi.otavanopisto.kuntaapi.server.persistence.dao.ClientOrganizationPermissionGrantDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.Client;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;

@ApplicationScoped
public class SecurityController {
  
  @Inject
  private ClientDAO clientDAO;

  @Inject
  private ClientOrganizationPermissionGrantDAO clientOrganizationPermissionGrantDAO;

  @Inject
  private IdentifierController identifierController;
  
  public Client findClientByClientIdAndSecret(String clientId, String clientSecret) {
    return clientDAO.findByClientIdAndClientSecret(clientId, clientSecret);
  }
  
  public boolean isUnrestrictedClient(Client client) {
    return client != null && client.getAccessType() == AccessType.UNRESTRICTED;
  }
  
  /**
   * Returns whether client has permission for organization
   * 
   * @param client client
   * @param organizationId organization id
   * @param permission permission
   * @return whether client has permission for organization
   */
  public boolean hasOrganizationPermission(Client client, OrganizationId organizationId, ClientOrganizationPermission permission) {
    Identifier organizationIdentifier = identifierController.findIdentifierById(organizationId);
    if (organizationIdentifier == null) {
      return false;
    }
    
    return clientOrganizationPermissionGrantDAO.findByClientOrganizationIdentifierAndPermission(client, organizationIdentifier, permission) != null;
  }
  
}
