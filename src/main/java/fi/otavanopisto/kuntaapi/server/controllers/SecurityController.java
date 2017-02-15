package fi.otavanopisto.kuntaapi.server.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.persistence.dao.ClientDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.Client;

@ApplicationScoped
public class SecurityController {
  
  @Inject
  private ClientDAO clientDAO;
  
  public Client findClientByClientIdAndSecret(String clientId, String clientSecret) {
    return clientDAO.findByClientIdAndClientSecret(clientId, clientSecret);
  }
  
  public boolean isUnrestrictedClient(Client client) {
    return client != null && client.getAccessType() == AccessType.UNRESTRICTED;
  }
  
}
