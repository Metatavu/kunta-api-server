package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.TileProvider;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Tile;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class TileController {

  @Inject
  private EntityController entityController;

  @Inject
  private Instance<TileProvider> tileProviders;

  public List<Tile> listTiles(OrganizationId organizationId) {
    List<Tile> result = new ArrayList<>();
    for (TileProvider tileProvider : getTileProviders()) {
      result.addAll(tileProvider.listOrganizationTiles(organizationId));
    }
    
    return entityController.sortEntitiesInNaturalOrder(result);
  }

  public Tile findTile(OrganizationId organizationId, TileId tileId) {
    for (TileProvider tileProvider : getTileProviders()) {
      Tile tile = tileProvider.findOrganizationTile(organizationId, tileId);
      if (tile != null) {
        return tile;
      }
    }
    
    return null;
  }

  public List<Attachment> listTileImages(OrganizationId organizationId, TileId tileId) {
    List<Attachment> result = new ArrayList<>();
    
    for (TileProvider tileProvider : getTileProviders()) {
      result.addAll(tileProvider.listOrganizationTileImages(organizationId, tileId));
    }

    return entityController.sortEntitiesInNaturalOrder(result);
  }

  public Attachment findTileImage(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId) {
    for (TileProvider tileProvider : getTileProviders()) {
      Attachment attachment = tileProvider.findTileImage(organizationId, tileId, attachmentId);
      if (attachment != null) {
        return attachment;
      }
    }
    
    return null;
  }
  
  public AttachmentData getTileImageData(OrganizationId organizationId, TileId tileId, AttachmentId attachmentId, Integer size) {
    for (TileProvider tileProvider : getTileProviders()) {
      AttachmentData attachmentData = tileProvider.getTileImageData(organizationId, tileId, attachmentId, size);
      if (attachmentData != null) {
        return attachmentData;
      }
    }
    
    return null;
  }
  
  private List<TileProvider> getTileProviders() {
    List<TileProvider> result = new ArrayList<>();
    
    Iterator<TileProvider> iterator = tileProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
