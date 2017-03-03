package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Tile;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.cache.TileCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementAttachmentCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.TileIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementTileEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private TileCache tileCache;
  
  @Inject
  private ManagementAttachmentCache managementAttachmentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private TileIdTaskQueue tileIdTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-tiles";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  private void executeNextTask() {
    IdTask<TileId> task = tileIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementTile(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteManagementTile(task.getId());
      }
    }
  }

  private void updateManagementTile(TileId managementTileId, Long orderIndex) {
    OrganizationId organizationId = managementTileId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Tile> response = api.wpV2TileIdGet(managementTileId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementTile(api, organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Finding organization %s tile failed on [%d] %s", managementTileId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementTile(DefaultApi api, OrganizationId organizationId, Tile managementTile, Long orderIndex) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementTile.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, tileId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    TileId tileKuntaApiId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Tile tile = managementTranslator.translateTile(tileKuntaApiId, managementTile);
    if (tile == null) {
      logger.severe(String.format("Could not translate management tile %s", identifier.getKuntaApiId()));
      return;
    }
    
    tileCache.put(tileKuntaApiId, tile);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(tile));
    
    List<AttachmentId> existingAttachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, tileKuntaApiId);

    if (managementTile.getFeaturedMedia() != null && managementTile.getFeaturedMedia() > 0) {
      AttachmentId attachmentId = updateFeaturedMedia(organizationId, identifier, api, managementTile.getFeaturedMedia());
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }

    for (AttachmentId existingAttachmentId : existingAttachmentIds) {
      identifierRelationController.removeChild(tileKuntaApiId, existingAttachmentId);
    }
  }
  
  private AttachmentId updateFeaturedMedia(OrganizationId organizationId, Identifier tileIdentifier, DefaultApi api, Integer featuredMedia) {
    ApiResponse<Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = managementTranslator.createManagementAttachmentId(organizationId, managementAttachment.getId(), ManagementConsts.ATTACHMENT_TYPE_TILE);
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementAttachmentId);
      identifierRelationController.addChild(tileIdentifier, identifier);

      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, ManagementConsts.ATTACHMENT_TYPE_TILE);
      if (attachment == null) {
        logger.severe(String.format("Could not translate management attachment %s", identifier.getKuntaApiId()));
        return null;
      }
      
      managementAttachmentCache.put(kuntaApiAttachmentId, attachment);
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
      
      return kuntaApiAttachmentId;
    }
  }
  
  private void deleteManagementTile(TileId managementTileId) {
    OrganizationId organizationId = managementTileId.getOrganizationId();
    Identifier tileIdentifier = identifierController.findIdentifierById(managementTileId);
    if (tileIdentifier != null) {
      TileId kuntaApiTileId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, tileIdentifier.getKuntaApiId());
      modificationHashCache.clear(tileIdentifier.getKuntaApiId());
      tileCache.clear(kuntaApiTileId);
      identifierController.deleteIdentifier(tileIdentifier);
    }
  }
  
  
}
