package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.TileProvider;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Tile;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.model.Attachment.MediaTypeEnum;

/**
 * Tile provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementTileProvider extends AbstractManagementProvider implements TileProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Override
  public List<Tile> listOrganizationTiles(OrganizationId organizationId) {
    String context = null;
    Integer page = null;
    Integer perPage = null;
    String search = null;
    LocalDateTime after = null;
    LocalDateTime before = null;
    List<String> exclude = null;
    List<String> include = null;
    Integer offset = null;
    String order = null; 
    String orderby = null;
    String slug = null;
    String status = null;
    String filter = null;

    ApiResponse<List<fi.metatavu.management.client.model.Tile>> response = managementApi.getApi(organizationId).wpV2TileGet(context, page, perPage, search, after,
        before, exclude, include, offset, order, orderby, slug, status, filter);

    if (!response.isOk()) {
      logger.severe(String.format("Tile listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateTiles(organizationId, response.getResponse());
    }
    
    return Collections.emptyList();
  }

  @Override
  public Tile findOrganizationTile(OrganizationId organizationId, TileId tileId) {
    fi.metatavu.management.client.model.Tile managementTile = findTileByTileId(organizationId, tileId);
    if (managementTile != null) {
      return translateTile(organizationId, managementTile);
    }
  
    return null;
  }

  @Override
  public List<Attachment> listOrganizationTileImages(OrganizationId organizationId, TileId tileId) {
    fi.metatavu.management.client.model.Tile managementTile = findTileByTileId(organizationId, tileId);
    if (managementTile != null) {
      Integer featuredMediaId = managementTile.getFeaturedMedia();
      if (featuredMediaId != null) {
        Attachment attachment = findManagementAttachment(organizationId, featuredMediaId);
        if (attachment != null) {
          return Collections.singletonList(attachment);  
        }
      }
    }
  
    return Collections.emptyList();
  }

  public Attachment findManagementAttachment(OrganizationId organizationId, Integer featuredMediaId) {
    fi.metatavu.management.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
    if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
      return translateAttachment(organizationId, featuredMedia);
    }
    
    return null;
  }

  @Override
  public Attachment findTileImage(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId) {
    fi.metatavu.management.client.model.Tile tile = findTileByTileId(organizationId, tileId);
    if (tile != null) {
      Integer featuredMediaId = tile.getFeaturedMedia();
      if (featuredMediaId != null) {
        AttachmentId managementAttachmentId = getImageAttachmentId(organizationId, featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.metatavu.management.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(organizationId, attachment);
        }
      }
    }
  
    return null;
  }

  @Override
  public AttachmentData getTileImageData(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId,
      Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.metatavu.management.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
    if (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE) {
      AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
      
      if (size != null) {
        return scaleImage(imageData, size);
      } else {
        return imageData;
      }
      
    }
    
    return null;
  }

  private fi.metatavu.management.client.model.Tile findTileByTileId(OrganizationId organizationId, TileId tileId) {
    TileId kuntaApiId = idController.translateTileId(tileId, ManagementConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Failed to convert %s into management tile id", tileId.toString()));
      return null;
    }
    
    ApiResponse<fi.metatavu.management.client.model.Tile> response = managementApi.getApi(organizationId).wpV2TileIdGet(kuntaApiId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding tile failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

  private List<Tile> translateTiles(OrganizationId organizationId, List<fi.metatavu.management.client.model.Tile> managementTiles) {
    List<Tile> result = new ArrayList<>();
    
    for (fi.metatavu.management.client.model.Tile managementTile : managementTiles) {
      result.add(translateTile(organizationId, managementTile));
    }
    
    return result;
  }

  private Tile translateTile(OrganizationId organizationId, fi.metatavu.management.client.model.Tile managementTile) {
    Tile tile = new Tile();
    
    TileId managementTileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementTile.getId()));
    TileId kuntaApiTileId = idController.translateTileId(managementTileId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiTileId == null) {
      logger.info(String.format("Found new news article %d", managementTile.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(managementTileId);
      kuntaApiTileId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    tile.setContents(managementTile.getContent().getRendered());
    tile.setId(kuntaApiTileId.getId());
    tile.setLink(managementTile.getTileLink());
    tile.setTitle(managementTile.getTitle().getRendered());
    
    return tile;
  }

}
