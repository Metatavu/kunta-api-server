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

  /**
   * Updates stored resource data
   * 
   * @param type type of stored resource
   * @param id id
   * @param data data
   */
  public void updateData(String type, BaseId id, String data) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      updateData(type, identifier, data);
    }
  }
  
  /**
   * Updates stored resource data
   * 
   * @param type type of stored resource
   * @param identifier identifier
   * @param data data
   */
  public void updateData(String type, Identifier identifier, String data) {
    StoredResource storedResource = findStoredResource(type, identifier);
    if (storedResource == null) {
      if (data != null) {
        createStoredResource(type, identifier, data);
      }
    } else {
      if (data == null) {
        deleteStoredResource(storedResource);
      } else {
        updateStoredResource(storedResource, data);
      }
    }
  }
  
  /**
   * Retrieves data from stored resource
   * 
   * @param type type of resource
   * @param id id
   * @return data
   */
  public String getData(String type, BaseId id) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      return getData(type, identifier);
    }
    
    return null;
  }

  /**
   * Retrieves data from stored resource
   * 
   * @param type type of resource
   * @param identifier identifier
   * @return data
   */
  public String getData(String type, Identifier identifier) {
    StoredResource storedResource = findStoredResource(type, identifier);
    if (storedResource == null) {
      return null;
    }
    
    return storedResource.getData();
  }
  
  private StoredResource findStoredResource(String type, Identifier identifier) {
    return storedResourceDAO.findByIdentifier(type, identifier);
  }
  
  private StoredResource createStoredResource(String type, Identifier identifier, String data) {
    return storedResourceDAO.create(identifier, type, data);
  }
  
  private StoredResource updateStoredResource(StoredResource storedResource, String data) {
    return storedResourceDAO.updateData(storedResource, data);
  }
  
  private void deleteStoredResource(StoredResource storedResource) {
    storedResourceDAO.delete(storedResource);
  }
  
}
