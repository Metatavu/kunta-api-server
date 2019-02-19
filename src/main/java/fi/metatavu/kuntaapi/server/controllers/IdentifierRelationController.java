package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.id.EmergencyId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PublicTransportAgencyId;
import fi.metatavu.kuntaapi.server.id.PublicTransportRouteId;
import fi.metatavu.kuntaapi.server.id.PublicTransportScheduleId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.metatavu.kuntaapi.server.id.PublicTransportTripId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.persistence.dao.IdentifierRelationDAO;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.persistence.model.IdentifierRelation;

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

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  /**
   * Adds a child reference for parent identifier
   * 
   * @param parentIdentifier
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
        logger.log(Level.SEVERE, () ->  String.format("Could not find identifier for parent id %s when setting parents", parentId));
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
    if (childIdentifier == null) {
      logger.log(Level.SEVERE, () -> String.format("Attempted to set parent (%s) identifier for null childIdentifier", parentIdentifier != null ? parentIdentifier.getKuntaApiId() : null));
      return;
    }
    
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
      Identifier relationParentIdentifier = identifierRelation.getParent();
      if ((relationParentIdentifier != null) && relationParentIdentifier.getId().equals(parentIdentifier.getId())) {
        parentFound = true;
      } else {
        try {
          identifierRelationDAO.delete(identifierRelation);
        } catch (Exception e) {
          
          if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, String.format("Failed to remove identifier relation %d", identifierRelation.getId()), e);
          }
          
          throw e;
        }
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
   * Lists page ids by child id
   * 
   * @param childId child id
   * @return page ids by child id
   */
  public List<PageId> listPageIdsByChildId(BaseId childId) {
    List<Identifier> identifiers = listParentIdentifiersByChildIdAndType(childId, IdType.PAGE);
    
    List<PageId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(PageId.class, identifier));
    }
    
    return result;
  }
  
  /**
   * Lists service ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return serviceIds by parent id
   */
  public List<ServiceId> listServiceIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.SERVICE);
    List<ServiceId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(ServiceId.class, identifier));
    }
    
    return result;
  }
  
  /**
   * Lists service ids by parent id. 
   * 
   * @param parentId parent id
   * @return serviceIds by parent id
   */
  public List<ServiceId> listServiceIdsByParentId(BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentAndType(parentId, IdType.SERVICE);
    List<ServiceId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(ServiceId.class, identifier));
    }
    
    return result;
  }
  
  /**
   * Lists service location service channel ids by parent id. 
   * 
   * @param parentId parent id
   * @return serviceLocationServiceChannelIds by parent id
   */
  public List<ServiceLocationServiceChannelId> listServiceLocationServiceChannelIdsByParentId(BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentAndType(parentId, IdType.SERVICE_LOCATION_SERVICE_CHANNEL);
    List<ServiceLocationServiceChannelId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, identifier));
    }
    
    return result;
  }
  
  /**
   * Lists announcement ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return announcement ids by parent id
   */
  public List<AnnouncementId> listAnnouncementIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.ANNOUNCEMENT);
    List<AnnouncementId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
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
   * Lists shortlink ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return shortlink ids by parent id
   */
  public List<ShortlinkId> listShortlinkIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.SHORTLINK);
    List<ShortlinkId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new ShortlinkId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists incident ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return incident ids by parent id
   */
  public List<IncidentId> listIncidentIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.INCIDENT);
    List<IncidentId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new IncidentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists emergency ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return emergency ids by parent id
   */
  public List<EmergencyId> listEmergencyIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.EMERGENCY);
    List<EmergencyId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new EmergencyId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists job ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return job ids by parent id
   */
  public List<JobId> listJobIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.JOB);
    List<JobId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new JobId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
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
   * Lists contact ids by source and parent id. 
   * 
   * @param parentId parent id
   * @return contact ids by parent id
   */
  public List<ContactId> listContactIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.CONTACT);
    List<ContactId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new ContactId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
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
  
  /**
   * Lists Public transport schedules ids by parent id. 
   * 
   * @param parentId parent id
   * @return transport agencies ids by parent id
   */
  public List<PublicTransportScheduleId> listPublicTransportScheduleIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_SCHEDULE);
    List<PublicTransportScheduleId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportScheduleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists Public transport route ids by parent id and source. 
   * 
   * @param source source type
   * @param parentId parent id
   * @return transport routes ids by parent id
   */
  public List<PublicTransportRouteId> listPublicTransportRouteIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_ROUTE);
    List<PublicTransportRouteId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportRouteId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists Public transport stop ids by parent id and source. 
   * 
   * @param source source type
   * @param parentId parent id
   * @return transport stops ids by parent id
   */
  public List<PublicTransportStopId> listPublicTransportStopIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_STOP);
    List<PublicTransportStopId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportStopId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists Public transport stopTime ids by parent id and source. 
   * 
   * @param source source type
   * @param parentId parent id
   * @return transport stopTimes ids by parent id
   */
  public List<PublicTransportStopTimeId> listPublicTransportStopTimeIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_STOPTIME);
    List<PublicTransportStopTimeId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportStopTimeId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists Public transport trip ids by parent id and source. 
   * 
   * @param source source type
   * @param parentId parent id
   * @return transport trips ids by parent id
   */
  public List<PublicTransportTripId> listPublicTransportTripIdsBySourceAndParentId(String source, BaseId parentId) {
    List<Identifier> identifiers = listChildIdentifiersByParentSourceAndType(parentId, source, IdType.PUBLIC_TRANSPORT_TRIP);
    List<PublicTransportTripId> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PublicTransportTripId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists child identifier by parent, source and type
   * 
   * @param parentIdentifier parent id
   * @param source source
   * @param type type
   * @return list identifier
   */
  private List<Identifier> listChildIdentifiersByParentSourceAndType(BaseId parentId, String source, IdType type) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, () -> String.format("Could not find identifier for parent id %s when listing a child ids", parentId));
      return Collections.emptyList();
    }
    
    return listChildIdentifiersByParentSourceAndType(parentIdentifier, source, type);    
  }

  /**
   * Lists child identifier by parent, source and type
   * 
   * @param parentIdentifier parent identifier
   * @param source source
   * @param type type
   * @return list identifier
   */
  private List<Identifier> listChildIdentifiersByParentSourceAndType(Identifier parentIdentifier, String source, IdType type) {
    return identifierRelationDAO.listChildIdentifiersByParentSourceAndType(parentIdentifier, source, type.name());
  }
  
  private List<Identifier> listChildIdentifiersByParentAndType(BaseId parentId, IdType type) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, () -> String.format("Could not find identifier for parent id %s when listing a child ids", parentId));
      return Collections.emptyList();
    }
    
    return identifierRelationDAO.listChildIdentifiersByParentAndType(parentIdentifier, type.name());    
  }
  
  private List<Identifier> listParentIdentifiersByChildIdAndType(BaseId childId, IdType type) {
    Identifier childIdentifier = identifierController.findIdentifierById(childId);
    if (childIdentifier == null) {
      logger.log(Level.WARNING, () -> String.format("Could not find identifier for child id %s when listing parent ids", childId));
      return Collections.emptyList();
    }
    
    return identifierRelationDAO.listParentIdentifiersByChildAndType(childIdentifier, type.name());    
  }
  
  

  public boolean isChildOf(BaseId parentId, BaseId childId) {
    return findIdentifierRelation(parentId, childId) != null;
  }

  private IdentifierRelation findIdentifierRelation(BaseId parentId, BaseId childId) {
    Identifier parentIdentifier = identifierController.findIdentifierById(parentId);
    if (parentIdentifier == null) {
      logger.log(Level.WARNING, () -> String.format("Could not find identifier for parent id %s", parentId));
      return null;
    }
    
    Identifier childIdentifier = identifierController.findIdentifierById(childId);
    if (childIdentifier == null) {
      logger.log(Level.WARNING, () -> String.format("Could not find identifier for child id %s ", childId));
      return null;
    }
    
    return identifierRelationDAO.findByParentAndChild(parentIdentifier, childIdentifier);
  }

  
}
