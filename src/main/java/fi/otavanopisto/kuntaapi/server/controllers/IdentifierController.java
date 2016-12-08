package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MissingOrganizationIdException;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.IdentifierDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Identifier controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class IdentifierController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierDAO identifierDAO;
  
  /**
   * Creates new identifier.
   * 
   * @param id identifier
   * @return created identifier
   */
  public Identifier createIdentifier(BaseId id) {
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
    return createIdentifier(id.getType().toString(), kuntaApiId, id.getSource(), id.getId(), organizationKuntaApiId);
  }
  
  public Identifier findIdentifierById(BaseId id) {
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
      logger.log(Level.SEVERE, String.format("Could not translate organization %s into Kunta API id"));
      return Collections.emptyList();
    }
    
    List<Identifier> identifiers = identifierDAO.listByOrganizationIdAndSourceAndType(organizationKuntaApiId, source, type);
    List<String> result = new ArrayList<>(identifiers.size());
    
    for (Identifier identifier : identifiers) {
      result.add(identifier.getSourceId());
    }
    
    return result;
  }

  public void deleteIdentifier(Identifier identifier) {
    identifierDAO.delete(identifier);
  }

  private Identifier createIdentifier(String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.create(type, kuntaApiId, source, sourceId, organizationKuntaApiId);
  }

  private Identifier findIdentifierByTypeSourceIdAndOrganizationId(String type, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.findByTypeSourceSourceIdAndOrganizationKuntaApiId(type, source, sourceId, organizationKuntaApiId);
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
