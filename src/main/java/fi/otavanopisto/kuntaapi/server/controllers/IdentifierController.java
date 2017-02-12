package fi.otavanopisto.kuntaapi.server.controllers;

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

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.MissingOrganizationIdException;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.IdentifierDAO;
import fi.otavanopisto.kuntaapi.server.persistence.dao.IdentifierRelationDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.IdentifierOrderIndex;
import fi.otavanopisto.kuntaapi.server.persistence.model.IdentifierRelation;

/**
 * Identifier controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class IdentifierController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierDAO identifierDAO;
  
  @Inject
  private IdentifierRelationDAO identifierRelationDAO;
  
  /**
   * Creates new identifier.
   * 
   * @param id identifier
   * @return created identifier
   */
  public Identifier createIdentifier(Long orderIndex, BaseId id) {
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
    List<String> organizationIds = listSourceIdsBySource(source, IdType.ORGANIZATION.toString());
    List<OrganizationId> result = new ArrayList<>(organizationIds.size());
    
    for (String organizationId : organizationIds) {
      result.add(new OrganizationId(source, organizationId));
    }
    
    return result;
  }
  
  public List<PageId> listOrganizationPageIdsBySource(OrganizationId organizationId, String source) {
    List<String> pageIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.PAGE.toString());
    List<PageId> result = new ArrayList<>(pageIds.size());
    
    for (String pageId : pageIds) {
      result.add(new PageId(organizationId, source, pageId));
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
  
  public List<ContactId> listOrganizationContactIdsBySource(OrganizationId organizationId, String source) {
    List<String> contactIds = listSourceIdsByOrganizationIdAndSourceAndType(organizationId, source, IdType.CONTACT.toString());
    List<ContactId> result = new ArrayList<>(contactIds.size());
    
    for (String contactId : contactIds) {
      result.add(new ContactId(organizationId, source, contactId));
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
  
  private List<String> listSourceIdsBySource(String source, String type) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, type);
    List<String> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(identifier.getSourceId());
    }
    
    return result;
  }

  private List<String> listSourceIdsByOrganizationIdAndSourceAndType(OrganizationId organizationId, String source, String type) {
    String organizationKuntaApiId = getOrganizationIdKuntaApiId(organizationId);
    if (organizationKuntaApiId == null) {
      logger.log(Level.SEVERE, String.format("Could not translate organization %s into Kunta API id", organizationId));
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByOrganizationIdAndSourceAndType(organizationKuntaApiId, source, type);
    List<String> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(identifier.getSourceId());
    }
    
    return result;
  }

  /**
   * Lists page ids by source and parent id
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return page ids by parent id
   */
  public List<PageId> listPageIdsBySourceAndParentId(String source, BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }

    List<Identifier> identifiers = identifierDAO.listBySourceParentAndTypeOrderByOrderIndex(source, parentIdentifier, IdType.PAGE.name());
    List<PageId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists attachment ids by source and parent id
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return attachment ids by parent id
   */
  public List<AttachmentId> listAttachmentIdsBySourceAndParentId(String source, BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }

    List<Identifier> identifiers = identifierDAO.listBySourceParentAndTypeOrderByOrderIndex(source, parentIdentifier, IdType.ATTACHMENT.name());
    List<AttachmentId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists fragment ids by parent id. 
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return fragment ids by parent id
   */
  public List<FragmentId> listFragmentIdsParentId(BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByParentAndTypeOrderByOrderIndex(parentIdentifier, IdType.FRAGMENT.name());
    List<FragmentId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists banner ids by parent id. 
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return banner ids by parent id
   */
  public List<BannerId> listBannerIdsParentId(BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByParentAndTypeOrderByOrderIndex(parentIdentifier, IdType.BANNER.name());
    List<BannerId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists tile ids by parent id. 
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return tile ids by parent id
   */
  public List<TileId> listTileIdsParentId(BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByParentAndTypeOrderByOrderIndex(parentIdentifier, IdType.TILE.name());
    List<TileId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  /**
   * Lists event ids by parent id. 
   * 
   * Results are sorted by orderIndex column
   * 
   * @param parentId parent id
   * @return event ids by parent id
   */
  public List<EventId> listEventIdsParentId(BaseId parentId) {
    Identifier parentIdentifier = findIdentifierById(parentId);
    if (parentIdentifier == null) {
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByParentAndTypeOrderByOrderIndex(parentIdentifier, IdType.EVENT.name());
    List<EventId> result = new ArrayList<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      result.add(new EventId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
    
    return result;
  }
  
  public List<ServiceId> listServiceIdsBySource(String source) {
    List<Identifier> identifiers = identifierDAO.listBySourceAndType(source, IdType.SERVICE.name());
    List<ServiceId> result = new ArrayList<>(identifiers.size());

    for (Identifier identifier : identifiers) {
      result.add(new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId()));
    }
 
    return result;
  }

  public void deleteIdentifier(Identifier identifier) {
    List<IdentifierRelation> identifierRelations = identifierRelationDAO.listByParentOrChild(identifier);
    for (IdentifierRelation identifierRelation : identifierRelations) {
      identifierRelationDAO.delete(identifierRelation);
    }
    
    identifierDAO.delete(identifier);
  }

  private Identifier createIdentifier(Long orderIndex, String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.create(null, orderIndex, type, kuntaApiId, source, sourceId, organizationKuntaApiId);
  }
  
  public Identifier updateIdentifier(Identifier identifier, Long orderIndex) {
    Identifier result = identifier;
    result = identifierDAO.updateOrderIndex(result, orderIndex);
    result = identifierDAO.updateModified(result, OffsetDateTime.now());
    return result;
  }

  private Identifier findIdentifierByTypeSourceIdAndOrganizationId(String type, String source, String sourceId, String organizationKuntaApiId) {
    if (StringUtils.equals(source, KuntaApiConsts.IDENTIFIER_NAME)) {
      return identifierDAO.findByTypeAndKuntaApiIdAndOrganizationKuntaApiId(type, sourceId, organizationKuntaApiId);
    } else {
      return identifierDAO.findByTypeSourceSourceIdAndOrganizationKuntaApiId(type, source, sourceId, organizationKuntaApiId);
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
