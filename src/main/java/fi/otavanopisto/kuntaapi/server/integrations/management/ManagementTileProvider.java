package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.model.Attachment.MediaTypeEnum;
import fi.otavanopisto.kuntaapi.server.cache.TileCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.TileProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementAttachmentCache;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Tile;

/**
 * Tile provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementTileProvider extends AbstractManagementProvider implements TileProvider {
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private TileCache tileCache;
  
  @Inject
  private ManagementAttachmentCache managementAttachmentCache;
  
  @Inject
  private ManagementImageLoader managementImageLoader;

  @Override
  public List<Tile> listOrganizationTiles(OrganizationId organizationId) {
    List<TileId> tileIds = identifierController.listTileIdsParentId(organizationId);
    List<Tile> result = new ArrayList<>(tileIds.size());
    
    for (TileId tileId : tileIds) {
      Tile tile = tileCache.get(tileId);
      if (tile != null) {
        result.add(tile);
      }
    }
    
    return result;
  }

  @Override
  public Tile findOrganizationTile(OrganizationId organizationId, TileId tileId) {
    return tileCache.get(tileId);
  }

  @Override
  public List<Attachment> listOrganizationTileImages(OrganizationId organizationId, TileId tileId) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsByParentId(organizationId, tileId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = managementAttachmentCache.get(attachmentId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }

  @Override
  public Attachment findTileImage(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId) {
    if (!identifierRelationController.isChildOf(tileId, attachmentId)) {
      return null;
    }
    
    return managementAttachmentCache.get(attachmentId);
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

}
