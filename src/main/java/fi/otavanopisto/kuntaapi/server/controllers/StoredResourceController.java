package fi.otavanopisto.kuntaapi.server.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.persistence.dao.StoredResourceDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.StoredResource;

@ApplicationScoped
public class StoredResourceController {
  
  @Inject
  private StoredResourceDAO storedResourceDAO;
  
  @Inject
  private IdentifierController identifierController;

  public void updateData(BaseId id, String data) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      updateData(identifier, data);
    }
  }
  
  public void updateData(Identifier identifier, String data) {
    StoredResource storedResource = findStoredResource(identifier);
    if (storedResource == null) {
      createStoredResource(identifier, data);
    } else {
      updateStoredResource(storedResource, data);
    }
  }
  
  public String getData(BaseId id) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      return getData(identifier);
    }
    
    return null;
  }
  
  public String getData(Identifier identifier) {
    StoredResource storedResource = findStoredResource(identifier);
    if (storedResource == null) {
      return null;
    }
    
    return storedResource.getData();
  }
  
  private StoredResource findStoredResource(Identifier identifier) {
    return storedResourceDAO.findByIdentifier(identifier);
  }
  
  private StoredResource createStoredResource(Identifier identifier, String data) {
    return storedResourceDAO.create(identifier, data);
  }
  
  private StoredResource updateStoredResource(StoredResource storedResource, String data) {
    return storedResourceDAO.updateData(storedResource, data);
  }
  
  
}
