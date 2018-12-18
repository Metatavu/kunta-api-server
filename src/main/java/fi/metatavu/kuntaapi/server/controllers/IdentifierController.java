package fi.metatavu.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.EmergencyId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.id.MissingOrganizationIdException;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationBaseId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.persistence.dao.ArchivedIdentifierDAO;
import fi.metatavu.kuntaapi.server.persistence.dao.IdentifierDAO;
import fi.metatavu.kuntaapi.server.persistence.dao.IdentifierRelationDAO;
import fi.metatavu.kuntaapi.server.persistence.model.ArchivedIdentifier;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.persistence.model.IdentifierOrderIndex;
import fi.metatavu.kuntaapi.server.persistence.model.IdentifierRelation;

/**
 * Identifier controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class IdentifierController {
  
  private static final String NULL_ORGANIZATION = "ROOT";

  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierDAO identifierDAO;
  
  @Inject
  private ArchivedIdentifierDAO archivedIdentifierDAO;
  
  @Inject
  private IdentifierRelationDAO identifierRelationDAO;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  public Identifier findIdentifierById(BaseId id) {
    if (id == null) {
      logger.severe("Passed null id to findIdentifierById method");
      return null;
    }
    
    String organizationKuntaApiId = null;
    if (id instanceof OrganizationBaseId) {
      organizationKuntaApiId = getOrganizationBaseIdOrganizationKuntaApiId((OrganizationBaseId) id);
    }
    
    return findIdentifierByTypeSourceAndIdOrganizationId(id.getType(), id.getSource(), id.getId(), organizationKuntaApiId);
  }
  
  /**
   * Acquires an identifier. 
   * 
   * 1) If identifier exists and is not archived, the identified is updated and returned
   * 2) If identifier is archived, the identifier is unarchived and returned
   * 3) If identifier does not exist, new identifier is created and returned
   * 
   * @param orderIndex orderIndex of index
   * @param id id
   * @return identifier
   */
  public Identifier acquireIdentifier(Long orderIndex, BaseId id) {
    Identifier identifier = findIdentifierById(id);
    if (identifier == null) {
      ArchivedIdentifier archivedIdentifier = findArchivedIdentifierById(id);
      if (archivedIdentifier != null) {
        identifier = createIdentifier(orderIndex == null ? archivedIdentifier.getOrderIndex() : orderIndex, archivedIdentifier.getType(), archivedIdentifier.getKuntaApiId(), archivedIdentifier.getSource(), archivedIdentifier.getSourceId(), archivedIdentifier.getOrganizationKuntaApiId());
        archivedIdentifierDAO.delete(archivedIdentifier);
      } else {
        identifier = createIdentifier(orderIndex == null ? 0l : orderIndex, id);
      }
    } else {  
      if (orderIndex != null) {
        identifier = updateIdentifier(identifier, orderIndex);
      }
    }
    
    return identifier;
  }

  public void deleteIdentifier(Identifier identifier) {
    List<IdentifierRelation> identifierRelations = identifierRelationDAO.listByParentOrChild(identifier);
    for (IdentifierRelation identifierRelation : identifierRelations) {
      identifierRelationDAO.delete(identifierRelation);
    }
    
    archivedIdentifierDAO.create(identifier.getType(), identifier.getKuntaApiId(), identifier.getSource(), identifier.getSourceId(), identifier.getOrganizationKuntaApiId(), identifier.getOrderIndex(), OffsetDateTime.now());
    identifierDAO.delete(identifier);
  }

  public Identifier findIdentifierByTypeSourceAndKuntaApiId(String type, String source, String kuntaApiId) {
    return identifierDAO.findByTypeSourceAndKuntaApiId(type, source, kuntaApiId);
  }

  public Identifier findIdentifierByTypeSourceAndKuntaApiId(IdType type, String source, String kuntaApiId) {
    return findIdentifierByTypeSourceAndKuntaApiId(type.toString(), source, kuntaApiId);
  }

  public Long getIdentifierOrderIndex(String kuntaApiId) {
    return identifierDAO.findOrderIndexByKuntaApiIds(kuntaApiId);
  }
  
  public Map<String, Long> getIdentifierOrderIndices(List<String> kuntaApiIds) {
    if (kuntaApiIds == null || kuntaApiIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, Long> result = new HashMap<>(kuntaApiIds.size());
    
    List<IdentifierOrderIndex> orderIndices = identifierDAO.listOrderIndicesByKuntaApiIds(kuntaApiIds);
    for (IdentifierOrderIndex orderIndex : orderIndices) {
      result.put(orderIndex.getKuntaApiId(), orderIndex.getOrderIndex());
    }
    
    for (String kuntaApiIdentifier : kuntaApiIds) {
      if (!result.containsKey(kuntaApiIdentifier)) {
        result.put(kuntaApiIdentifier, Long.MAX_VALUE);
      }
    }
    
    return result;
  }
  
  /**
   * Lists organization ids by source. Returned ids are not coverted into KuntaAPI ids
   * 
   * @param source source
   * @return Lists of organization ids
   */
  public List<OrganizationId> listOrganizationsBySource(String source) {
    return listOrganizationsBySource(source, null, null);
  }
  
  /**
   * Lists organization ids by source. Returned ids are not coverted into KuntaAPI ids
   * 
   * @param source source
   * @param firstResult first result
   * @param maxResults max results
   * @return Lists of organization ids
   */
  public List<OrganizationId> listOrganizationsBySource(String source, Integer firstResult, Integer maxResults) {
    List<String> organizationIds = listSourceIdsBySource(source, IdType.ORGANIZATION.toString(), firstResult, maxResults);
    List<OrganizationId> result = new ArrayList<>(organizationIds.size());
    
    for (String organizationId : organizationIds) {
      result.add(new OrganizationId(source, organizationId));
    }
    
    return result;
  }

  
  /**
   * Lists page ids by source. Method returns page ids from all organizations
   * 
   * @param source source
   * @param firstResult first result
   * @param maxResults max results
   * @return Lists of page ids
   */
  public List<PageId> listPageIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.PAGE.toString(), firstResult, maxResults);
    List<PageId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(PageId.class, identifier));
    }
 
    return result;
  }
  
  public List<PageId> listOrganizationPageIdsBySource(OrganizationId organizationId, String source) {
    return listOrganizationPageIdsBySource(organizationId, source, null, null);
  }
  
  public List<PageId> listOrganizationPageIdsBySource(OrganizationId organizationId, String source, Integer firstResult, Integer maxResults) {
    List<String> pageIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.PAGE.toString(), firstResult, maxResults);
    List<PageId> result = new ArrayList<>(pageIds.size());
    
    for (String pageId : pageIds) {
      result.add(new PageId(organizationId, source, pageId));
    }
    
    return result;
  }
  
  public Long countOrganizationPageIdsBySource(OrganizationId organizationId, String source) {
    return countSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.PAGE.toString());
  }
  
  public List<AnnouncementId> listOrganizationAnnouncementIdsBySource(OrganizationId organizationId, String source) {
    List<String> announcementIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.ANNOUNCEMENT.toString());
    List<AnnouncementId> result = new ArrayList<>(announcementIds.size());
    
    for (String announcementId : announcementIds) {
      result.add(new AnnouncementId(organizationId, source, announcementId));
    }
    
    return result;
  }
  
  public List<BannerId> listOrganizationBannerIdsBySource(OrganizationId organizationId, String source) {
    List<String> bannerIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.BANNER.toString());
    List<BannerId> result = new ArrayList<>(bannerIds.size());
    
    for (String bannerId : bannerIds) {
      result.add(new BannerId(organizationId, source, bannerId));
    }
    
    return result;
  }
  
  public List<FragmentId> listOrganizationFragmentIdsBySource(OrganizationId organizationId, String source) {
    List<String> fragmentIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.FRAGMENT.toString());
    List<FragmentId> result = new ArrayList<>(fragmentIds.size());
    
    for (String fragmentId : fragmentIds) {
      result.add(new FragmentId(organizationId, source, fragmentId));
    }
    
    return result;
  }

  public List<ShortlinkId> listOrganizationShortlinkIdsBySource(OrganizationId organizationId, String source) {
    List<String> shortlinkIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.SHORTLINK.toString());
    List<ShortlinkId> result = new ArrayList<>(shortlinkIds.size());
    
    for (String shortlinkId : shortlinkIds) {
      result.add(new ShortlinkId(organizationId, source, shortlinkId));
    }
    
    return result;
  }

  public List<IncidentId> listOrganizationIncidentIdsBySource(OrganizationId organizationId, String source) {
    List<String> incidentIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.INCIDENT.toString());
    List<IncidentId> result = new ArrayList<>(incidentIds.size());
    
    for (String incidentId : incidentIds) {
      result.add(new IncidentId(organizationId, source, incidentId));
    }
    
    return result;
  }

  public List<EventId> listOrganizationEventIdsBySource(OrganizationId organizationId, String source, Integer firstResult, Integer maxResults) {
    List<String> eventIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.EVENT.toString(), firstResult, maxResults);
    List<EventId> result = new ArrayList<>(eventIds.size());
    
    for (String eventId : eventIds) {
      result.add(new EventId(organizationId, source, eventId));
    }
    
    return result;
  }

  /**
   * Lists job ids by organization id. Returned ids are in source format
   * 
   * @param organizationId organization id
   * @param source source
   * @param firstResult first result
   * @param maxResults max results
   * @return list of job ids in source format
   */
  public List<JobId> listOrganizationJobIdsBySource(OrganizationId organizationId, String source, Integer firstResult, Integer maxResults) {
    List<String> jobIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.JOB.toString(), firstResult, maxResults);
    List<JobId> result = new ArrayList<>(jobIds.size());
    
    for (String jobId : jobIds) {
      result.add(new JobId(organizationId, source, jobId));
    }
    
    return result;
  }

  /**
   * Lists job ids by organization id. Returned ids are in source format
   * 
   * @param organizationId organization id
   * @param source source
   * @return list of job ids in source format
   */
  public List<JobId> listOrganizationJobIdsBySource(OrganizationId organizationId, String source) {
    return listOrganizationJobIdsBySource(organizationId, source, null, null);
  }

  public List<EmergencyId> listOrganizationEmergencyIdsBySource(OrganizationId organizationId, String source) {
    List<String> emergencyIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.EMERGENCY.toString());
    List<EmergencyId> result = new ArrayList<>(emergencyIds.size());
    
    for (String emergencyId : emergencyIds) {
      result.add(new EmergencyId(organizationId, source, emergencyId));
    }
    
    return result;
  }
  
  public List<ContactId> listOrganizationContactIdsBySource(OrganizationId organizationId, String source) {
    List<String> contactIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.CONTACT.toString());
    List<ContactId> result = new ArrayList<>(contactIds.size());
    
    for (String contactId : contactIds) {
      result.add(new ContactId(organizationId, source, contactId));
    }
    
    return result;
  }

  /**
   * Lists new article ids by source. Method returns news article ids from all organizations
   * 
   * @param source source
   * @param firstResult first result
   * @param maxResults max results
   * @return Lists of page ids
   */
  public List<NewsArticleId> listNewsArticleIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.NEWS_ARTICLE.toString(), firstResult, maxResults);
    List<NewsArticleId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(NewsArticleId.class, identifier));
    }
 
    return result;
  }

  public List<NewsArticleId> listOrganizationNewsArticleIdsBySource(OrganizationId organizationId, String source) {
    List<String> newsArticleIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.NEWS_ARTICLE.toString());
    List<NewsArticleId> result = new ArrayList<>(newsArticleIds.size());
    
    for (String newsArticleId : newsArticleIds) {
      result.add(new NewsArticleId(organizationId, source, newsArticleId));
    }
    
    return result;
  }

  public List<MenuId> listOrganizationMenuIdsBySource(OrganizationId organizationId, String source) {
    List<String> menuIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.MENU.toString());
    List<MenuId> result = new ArrayList<>(menuIds.size());
    
    for (String menuId : menuIds) {
      result.add(new MenuId(organizationId, source, menuId));
    }
    
    return result;
  }

  public List<MenuItemId> listOrganizationMenuItemIdsBySource(OrganizationId organizationId, String source) {
    List<String> menuItemIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.MENU_ITEM.toString());
    List<MenuItemId> result = new ArrayList<>(menuItemIds.size());
    
    for (String menuItemId : menuItemIds) {
      result.add(new MenuItemId(organizationId, source, menuItemId));
    }
    
    return result;
  }

  public List<TileId> listOrganizationTileIdsBySource(OrganizationId organizationId, String source) {
    List<String> tileIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.TILE.toString());
    List<TileId> result = new ArrayList<>(tileIds.size());
    
    for (String tileId : tileIds) {
      result.add(new TileId(organizationId, source, tileId));
    }
    
    return result;
  }
  
  private List<String> listSourceIdsBySource(String source, String type, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, type, firstResult, maxResults);
    List<String> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(identifier.getSourceId());
    }
    
    return result;
  }

  private List<String> listSourceIdsByOrganizationIdAndSourceAndType(OrganizationId organizationId, String source, String type) {
    return listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, type, null, null);
  }
  
  private List<String> listSourceIdsByOrganizationIdAndSourceAndType(OrganizationId organizationId, String source, String type, Integer firstResult, Integer maxResults) {
    String organizationKuntaApiId = getOrganizationIdKuntaApiId(organizationId);
    if (organizationKuntaApiId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", organizationId));
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByOrganizationIdAndSourceAndType(organizationKuntaApiId, source, type, firstResult, maxResults);
    List<String> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(identifier.getSourceId());
    }
    
    return result;
  }
  
  private Long countSourceIdsByOrganizationIdAndSourceAndType(OrganizationId organizationId, String source, String type) {
    String organizationKuntaApiId = getOrganizationIdKuntaApiId(organizationId);
    if (organizationKuntaApiId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", organizationId));
      return 0l;
    }
    
    return identifierDAO.countByOrganizationIdAndSourceAndType(organizationKuntaApiId, source, type);
  }
  
  public List<ServiceId> listServiceIdsBySource(String source) {
    return listServiceIdsBySource(source, (Integer) null, (Integer) null);
  }
  
  public List<ServiceId> listServiceIdsBySource(String source, Long firstResult, Long maxResults) {
    return listServiceIdsBySource(source, firstResult != null ? firstResult.intValue() : null, maxResults != null ? maxResults.intValue() : null);
  }
  
  public List<ServiceId> listServiceIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.SERVICE.name(), firstResult, maxResults);
    List<ServiceId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
 
    return result;
  }
  
  public List<OrganizationId> listOrganizationIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.ORGANIZATION.name(), firstResult, maxResults);
    List<OrganizationId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(OrganizationId.class, identifier));
    }
 
    return result;
  }
  
  public List<ElectronicServiceChannelId> listElectronicServiceChannelIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.ELECTRONIC_SERVICE_CHANNEL.name(), firstResult, maxResults);
    List<ElectronicServiceChannelId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, identifier));
    }
 
    return result;
  }
  
  public List<ServiceLocationServiceChannelId> listServiceLocationServiceChannelIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.SERVICE_LOCATION_SERVICE_CHANNEL.name(), firstResult, maxResults);
    List<ServiceLocationServiceChannelId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, identifier));
    }
 
    return result;
  }
  
  public List<PhoneServiceChannelId> listPhoneServiceChannelIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.PHONE_SERVICE_CHANNEL.name(), firstResult, maxResults);
    List<PhoneServiceChannelId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, identifier));
    }
 
    return result;
  }
  
  public List<PrintableFormServiceChannelId> listPrintableFormServiceChannelIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.PRINTABLE_FORM_SERVICE_CHANNEL.name(), firstResult, maxResults);
    List<PrintableFormServiceChannelId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, identifier));
    }
 
    return result;
  }
  
  public List<WebPageServiceChannelId> listWebPageServiceChannelIdsBySource(String source, Integer firstResult, Integer maxResults) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.WEBPAGE_SERVICE_CHANNEL.name(), firstResult, maxResults);
    List<WebPageServiceChannelId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, identifier));
    }
 
    return result;
  }
  
  private ArchivedIdentifier findArchivedIdentifierById(BaseId id) {
    if (id == null) {
      return null;
    }
    
    String organizationKuntaApiId = null;
    if (id instanceof OrganizationBaseId) {
      organizationKuntaApiId = getOrganizationBaseIdOrganizationKuntaApiId((OrganizationBaseId) id);
    }
    
    return findArchivedIdentifierByTypeSourceIdAndOrganizationId(id.getType().name(), id.getSource(), id.getId(), organizationKuntaApiId);
  }
  
  private ArchivedIdentifier findArchivedIdentifierByTypeSourceIdAndOrganizationId(String type, String source, String sourceId, String organizationKuntaApiId) {
    if (StringUtils.equals(source, KuntaApiConsts.IDENTIFIER_NAME)) {
      return archivedIdentifierDAO.findByTypeAndKuntaApiIdAndOrganizationKuntaApiId(type, sourceId, organizationKuntaApiId);
    } else {
      return archivedIdentifierDAO.findByTypeSourceSourceIdAndOrganizationKuntaApiId(type, source, sourceId, organizationKuntaApiId);
    }
  }
  
  /**
   * Creates new identifier.
   * 
   * @param id identifier
   * @return created identifier
   */
  private Identifier createIdentifier(Long orderIndex, BaseId id) {
    String organizationKuntaApiId = null;
    if (id instanceof OrganizationBaseId) {
      OrganizationBaseId organizationBaseId = (OrganizationBaseId) id;
      if (organizationBaseId.getOrganizationId() == null) {
        throw new MissingOrganizationIdException("Attempted to create organizationBaseId without organization");
      }

      organizationKuntaApiId = getOrganizationBaseIdOrganizationKuntaApiId(organizationBaseId);
      if (organizationKuntaApiId == null) {
        throw new MissingOrganizationIdException(String.format("Could not find organiztion %s for id %s", organizationBaseId.getOrganizationId().toString(), organizationBaseId.toString()));
      }
    }
    
    String kuntaApiId = UUID.randomUUID().toString();
    return createIdentifier(orderIndex, id.getType().toString(), kuntaApiId, id.getSource(), id.getId(), organizationKuntaApiId);
  }

  private Identifier createIdentifier(Long orderIndex, String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.create(orderIndex, type, kuntaApiId, source, sourceId, organizationKuntaApiId == null ? NULL_ORGANIZATION : organizationKuntaApiId, OffsetDateTime.now());
  }
  
  private Identifier updateIdentifier(Identifier identifier, Long orderIndex) {
    Identifier result = identifier;
    result = identifierDAO.updateOrderIndex(result, orderIndex);
    result = identifierDAO.updateModified(result, OffsetDateTime.now());
    return result;
  }

  private Identifier findIdentifierByTypeSourceIdAndOrganizationId(String type, String source, String sourceId, String organizationKuntaApiId) {
    if (StringUtils.equals(source, KuntaApiConsts.IDENTIFIER_NAME)) {
      return identifierDAO.findByTypeAndKuntaApiIdAndOrganizationKuntaApiId(type, sourceId, organizationKuntaApiId == null ? NULL_ORGANIZATION : organizationKuntaApiId);
    } else {
      return identifierDAO.findByTypeSourceSourceIdAndOrganizationKuntaApiId(type, source, sourceId, organizationKuntaApiId == null ? NULL_ORGANIZATION : organizationKuntaApiId);
    }
  }

  private Identifier findIdentifierByTypeSourceAndIdOrganizationId(IdType type, String source, String sourceId, String organizationKuntaApiId) {
    return findIdentifierByTypeSourceIdAndOrganizationId(type.toString(), source, sourceId, organizationKuntaApiId);
  }
  
  private String getOrganizationBaseIdOrganizationKuntaApiId(OrganizationBaseId organizationBaseId) {
    OrganizationId organizationId = organizationBaseId.getOrganizationId();
    if (KuntaApiConsts.IDENTIFIER_NAME.equals(organizationId.getSource())) {
      return organizationId.getId();
    }
    
    return getOrganizationIdKuntaApiId(organizationId);
  }

  private String getOrganizationIdKuntaApiId(OrganizationId organizationId) {
    Identifier organizationIdentifier = findIdentifierByTypeSourceAndIdOrganizationId(organizationId.getType(), organizationId.getSource(), organizationId.getId(), null);
    if (organizationIdentifier != null) {
      return organizationIdentifier.getKuntaApiId();
    }
    
    return null;
  }

}
