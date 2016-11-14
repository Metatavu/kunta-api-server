package fi.otavanopisto.kuntaapi.server.integrations.mwp;

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
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementApi;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementImageLoader;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Tile;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;

/**
 * Tile provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class MwpTileProvider extends AbstractMwpProvider implements TileProvider {
  
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

    ApiResponse<List<fi.otavanopisto.mwp.client.model.Tile>> response = managementApi.getApi(organizationId).wpV2TileGet(context, page, perPage, search, after,
        before, exclude, include, offset, order, orderby, slug, status, filter);

    if (!response.isOk()) {
      logger.severe(String.format("Tile listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateTiles(response.getResponse());
    }
    
    return Collections.emptyList();
  }

  @Override
  public Tile findOrganizationTile(OrganizationId organizationId, TileId tileId) {
    fi.otavanopisto.mwp.client.model.Tile mwpTile = findTileByTileId(organizationId, tileId);
    if (mwpTile != null) {
      return translateTile(mwpTile);
    }
  
    return null;
  }

  @Override
  public List<Attachment> listOrganizationTileImages(OrganizationId organizationId, TileId tileId) {
    fi.otavanopisto.mwp.client.model.Tile mwpTile = findTileByTileId(organizationId, tileId);
    if (mwpTile != null) {
      Integer featuredMediaId = mwpTile.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(featuredMedia));
        }
      }
    }
  
    return Collections.emptyList();
  }

  @Override
  public Attachment findTileImage(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId) {
    fi.otavanopisto.mwp.client.model.Tile tile = findTileByTileId(organizationId, tileId);
    if (tile != null) {
      Integer featuredMediaId = tile.getFeaturedMedia();
      if (featuredMediaId != null) {
        AttachmentId mwpAttachmentId = getImageAttachmentId(featuredMediaId);
        if (!idController.idsEqual(attachmentId, mwpAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(attachment);
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
    
    fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
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

  private fi.otavanopisto.mwp.client.model.Tile findTileByTileId(OrganizationId organizationId, TileId tileId) {
    TileId kuntaApiId = idController.translateTileId(tileId, MwpConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Failed to convert %s into MWP id", tileId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Tile> response = managementApi.getApi(organizationId).wpV2TileIdGet(kuntaApiId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding tile failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

  private List<Tile> translateTiles(List<fi.otavanopisto.mwp.client.model.Tile> mwpTiles) {
    List<Tile> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Tile mwpTile : mwpTiles) {
      result.add(translateTile(mwpTile));
    }
    
    return result;
  }

  private Tile translateTile(fi.otavanopisto.mwp.client.model.Tile mwpTile) {
    Tile tile = new Tile();
    
    TileId mwpId = new TileId(MwpConsts.IDENTIFIER_NAME, String.valueOf(mwpTile.getId()));
    TileId kuntaApiId = idController.translateTileId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new news article %d", mwpTile.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(mwpId);
      kuntaApiId = new TileId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    tile.setContents(mwpTile.getContent().getRendered());
    tile.setId(kuntaApiId.getId());
    tile.setLink(mwpTile.getTileLink());
    tile.setTitle(mwpTile.getTitle().getRendered());
    
    return tile;
  }

}
