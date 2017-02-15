package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
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
  
  /**
   * Adds a child reference for parent identifier
   * 
   * @param parentId parent identifier
   * @param childIdentifier child identifier
   */
  public void addChild(Identifier parentIdentifier, Identifier childIdentifier) {
    if (identifierRelationDAO.findByParentAndChild(parentIdentifier, childIdentifier) == null) {
      identifierRelationDAO.create(parentIdentifier, childIdentifier);
    }
  }
  
  /**
   * Removes a child reference from parent
   * 
   * @param parentId parent id
   * @param childId child id
   */
  public void removeChild(BaseId parentId, BaseId childId) {
    IdentifierRelation identifierRelation = findIdentifierRelation(parentId, childId);
    if (identifierRelation != null) {
      identifierRelationDAO.delete(identifierRelation);
    }
  }
  
  /**
   * Sets a parent for given identifier. 
   * 
   * All existing parent relations are removed in this process. 
   * 
   * @param childIdentifier identifier
   * @param parentId id of new parent or null to remove all parent relations
   */
  public void setParentId(Identifier childIdentifier, BaseId parentId) {
    Identifier parentIdentifier = null;
    
    if (parentId != null) {
      parentIdentifier = identifierController.findIdentifierById(parentId);
      if (parentIdentifier == null) {
        logger.log(Level.SEVERE, String.format("Could not find identifier for parent id %s when setting parents", parentId));
        return;
      }
    }
    
    setParentIdentifier(childIdentifier, parentIdentifier);
  }

  /**
   * Sets a parent for given identifier. 
   * 
   * All existing parent relations are removed in this process. 
   * 
   * @param childIdentifier identifier
   * @param parentIdentifier identifier of new parent or null to remove all parent relations
   */
  public void setParentIdentifier(Identifier childIdentifier, Identifier parentIdentifier) {
    if (parentIdentifier == null) {
      removeParentIdentifierRelations(childIdentifier);
    } else {
      setParentIdentifierRelation(parentIdentifier, childIdentifier);
    }
  }

  private void removeParentIdentifierRelations(Identifier childIdentifier) {
    for (IdentifierRelation identifierRelation : identifierRelationDAO.listByChild(childIdentifier)) {
      identifierRelationDAO.delete(identifierRelation);
    }
  }

  private void setParentIdentifierRelation(Identifier parentIdentifier, Identifier childIdentifier) {
    boolean parentFound = false;
    
    for (IdentifierRelation identifierRelation : identifierRelationDAO.listByChild(childIdentifier)) {
      if (identifierRelation.getParent().getId().equals(parentIdentifier.getId())) {
        parentFound = true;
      } else {
        identifierRelationDAO.delete(identifierRelation);
      }
    }
 
    if (!parentFound) {
      identifierRelationDAO.create(parentIdentifier, childIdentifier);
    }
  }
  

  /**
   * Lists page ids by source and parent id
   * 
   * @param parentId parent id
   * @return page ids by parent id
   */
  public List<PageId> listPageIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PAGE);
    List<PageId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists fragment ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return fragment ids by parent id
   */
  public List<FragmentId> listFragmentIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.FRAGMENT);
    List<FragmentId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists banner ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return banner ids by parent id
   */
  public List<BannerId> listBannerIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.BANNER);
    List<BannerId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists tile ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return tile ids by parent id
   */
  public List<TileId> listTileIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.TILE);
    List<TileId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists event ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return event ids by parent id
   */
  public List<EventId> listEventIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.EVENT);
    List<EventId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new EventId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }

  /**
   * Lists attachment ids by parent id. 
   * 
   * @param parentId parent id
   * @return event ids by parent id
   */
  public List<AttachmentId> listAttachmentIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.ATTACHMENT);
    List<AttachmentId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists menu ids by parent id. 
   * 
   * @param parentId parent id
   * @return event ids by parent id
   */
  public List<MenuId> listMenuIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.MENU);
    List<MenuId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists menuItem ids by parent id. 
   * 
   * @param parentId parent id
   * @return menu item ids by parent id
   */
  public List<MenuItemId> listMenuItemIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.MENU_ITEM);
    List<MenuItemId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new MenuItemId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists Public transport agencies ids by parent id. 
   * 
   * @param parentId parent id
   * @return transport agencies ids by parent id
   */
  public List<PublicTransportAgencyId> listPublicTransportAgencyIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_AGENCY);
    List<PublicTransportAgencyId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportAgencyId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  private List<Identifier> listChildIdentifiersByParentSourceAndType(BaseId parentId, String source, IdType type) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, String.format("Could not find identifier for parent id %s when listing a child ids", parentId));
      return Collections.emptyList();
    }
    
    return identifierRelationDAO.listChildIdentifiersByParentSourceAndType(parentIdentifier, source, type.name());    
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
