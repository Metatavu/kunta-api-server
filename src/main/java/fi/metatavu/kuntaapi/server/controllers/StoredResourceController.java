package fi.metatavu.kuntaapi.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.persistence.dao.StoredBinaryResourceDAO;
import fi.metatavu.kuntaapi.server.persistence.dao.StoredResourceDAO;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.persistence.model.StoredBinaryResource;
import fi.metatavu.kuntaapi.server.persistence.model.StoredResource;
import fi.metatavu.kuntaapi.server.resources.StoredBinaryData;

@ApplicationScoped
public class StoredResourceController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private StoredResourceDAO storedResourceDAO;
  
  @Inject
  private StoredBinaryResourceDAO storedBinaryResourceDAO;
  
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
   * Updates stored binary resource data
   * 
   * @param type type of stored binary resource
   * @param id id
   * @param data data
   */
  public void updateBinaryData(String type, BaseId id, StoredBinaryData data) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      updateBinaryData(type, identifier, data);
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
   * Updates stored binary resource data
   * 
   * @param type type of stored resource
   * @param identifier identifier
   * @param data data
   */
  public void updateBinaryData(String type, Identifier identifier, StoredBinaryData data) {
    StoredBinaryResource storedBinaryResource = findStoredBinaryResource(type, identifier);
    if (storedBinaryResource == null) {
      if (data != null) {
        createStoredBinaryResource(type, identifier, data);
      }
    } else {
      if (data == null) {
        deleteStoredBinaryResource(storedBinaryResource);
      } else {
        updateStoredBinaryResource(storedBinaryResource, data);
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
   * Retrieves data from stored binary resource
   * 
   * @param type type of resource
   * @param id id
   * @return data stream
   */
  public StoredBinaryData getBinaryData(String type, BaseId id) {
    Identifier identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      return getBinaryData(type, identifier);
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

  /**
   * Retrieves data from stored binary resource
   * 
   * @param type type of resource
   * @param identifier identifier
   * @return data stream
   */
  public StoredBinaryData getBinaryData(String type, Identifier identifier) {
    StoredBinaryResource storedBinarResource = findStoredBinaryResource(type, identifier);
    if (storedBinarResource == null) {
      return null;
    }
    
    return new StoredBinaryData(storedBinarResource.getContentType(), new ByteArrayInputStream(storedBinarResource.getData()));
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
  
  private StoredBinaryResource findStoredBinaryResource(String type, Identifier identifier) {
    return storedBinaryResourceDAO.findByIdentifier(type, identifier);
  }
  
  private StoredBinaryResource createStoredBinaryResource(String type, Identifier identifier, StoredBinaryData data) {
    try {
      return storedBinaryResourceDAO.create(identifier, type, data.getContentType(), IOUtils.toByteArray(data.getDataStream()));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read input stream into byte array", e);
    }
    
    return null;
  }
  
  private StoredBinaryResource updateStoredBinaryResource(StoredBinaryResource storedBinaryResource, StoredBinaryData data) {
    try {
      storedBinaryResourceDAO.updateContentType(storedBinaryResource, data.getContentType());
      return storedBinaryResourceDAO.updateData(storedBinaryResource, IOUtils.toByteArray(data.getDataStream()));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read input stream into byte array", e);
    }
    
    return null;
  }
  
  private void deleteStoredBinaryResource(StoredBinaryResource storedBinaryResource) {
    storedBinaryResourceDAO.delete(storedBinaryResource);
  }
  
}
