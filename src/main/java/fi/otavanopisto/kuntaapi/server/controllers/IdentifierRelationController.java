package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.IdentifierRelationDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.IdentifierRelation;

/**
 * Identifier relation controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class IdentifierRelationController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierRelationDAO identifierRelationDAO;
  
  @Inject
  private IdentifierController identifierController;
  
  public void addChild(BaseId parentId, BaseId childId) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.SEVERE, String.format("Could not find identifier for parent id %s when adding a child relation", parentId));
      return;
    }
    
    Identifier childIdentifier = identifierController.findIdentifierById(childId);
    if (childIdentifier == null) {
      logger.log(Level.SEVERE, String.format("Could not find identifier for child id %s when adding a child relation", childId));
      return;
    }
    
    if (identifierRelationDAO.findByParentAndChild(parentIdentifier, childIdentifier) == null) {
      identifierRelationDAO.create(parentIdentifier, childIdentifier);
    }
  }
  
  public void removeChild(BaseId parentId, BaseId childId) {
    IdentifierRelation identifierRelation = findIdentifierRelation(parentId, childId);
    if (identifierRelation != null) {
      identifierRelationDAO.delete(identifierRelation);
    }
  }
  
  public List<AttachmentId> listAttachmentIdsByParentId(OrganizationId organizationId, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByType(parentId, IdType.ATTACHMENT);
    List<AttachmentId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  private List<Identifier> listChildIdentifiersByType(BaseId parentId, IdType type) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, String.format("Could not find identifier for parent id %s when listing a child ids", parentId));
      return Collections.emptyList();
    }
    
    return identifierRelationDAO.listChildIdentifiersByParentAndType(parentIdentifier, type.name());    
  }

  public boolean isChildOf(BaseId parentId, BaseId childId) {
    return findIdentifierRelation(parentId, childId) != null;
  }

  private IdentifierRelation findIdentifierRelation(BaseId parentId, BaseId childId) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, String.format("Could not find identifier for parent id %s", parentId));
      return null;
    }
    
    Identifier childIdentifier = identifierController.findIdentifierById(childId);
    if (childIdentifier == null) {
      logger.log(Level.WARNING, String.format("Could not find identifier for child id %s ", childId));
      return null;
    }
    
    return identifierRelationDAO.findByParentAndChild(parentIdentifier, childIdentifier);
  }
  
}
