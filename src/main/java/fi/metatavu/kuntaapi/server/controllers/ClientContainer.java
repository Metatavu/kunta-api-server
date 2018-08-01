package fi.metatavu.kuntaapi.server.controllers;

import javax.enterprise.context.RequestScoped;

import fi.metatavu.kuntaapi.server.persistence.model.clients.Client;

@RequestScoped
public class ClientContainer {

  private Client client;
  
  public void setClient(Client client) {
    this.client = client;
  }
  
  public Client getClient() {
    return client;
  }
  
}
