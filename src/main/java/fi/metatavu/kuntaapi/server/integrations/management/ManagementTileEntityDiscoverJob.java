package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Tile;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.TileIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.resources.TileResourceContainer;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementTileEntityDiscoverJob extends EntityDiscoverJob<IdTask<TileId>> {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 
  
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
  private TileResourceContainer tileCache;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentCache;

  @Inject
  private ManagementAttachmentDataResourceContainer managementAttachmentDataResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private TileIdTaskQueue tileIdTaskQueue;

  @Inject
  private ScaledImageStore scaledImageStore;

  @Override
  public String getName() {
    return "management-tiles";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<TileId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementTile(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementTile(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<TileId> task = tileIdTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }

  private void updateManagementTile(TileId managementTileId, Long orderIndex) {
    OrganizationId organizationId = managementTileId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Tile> response = api.wpV2TileIdGet(managementTileId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementTile(api, organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(() -> String.format("Finding organization %s tile failed on [%d] %s", managementTileId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementTile(DefaultApi api, OrganizationId organizationId, Tile managementTile, Long orderIndex) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementTile.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, tileId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    TileId tileKuntaApiId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Tile tile = managementTranslator.translateTile(tileKuntaApiId, managementTile);
    if (tile == null) {
      logger.severe(() -> String.format("Could not translate management tile %s", identifier.getKuntaApiId()));
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
      logger.severe(() -> String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
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
        logger.log(Level.SEVERE, () -> String.format("Could not translate management attachment %s", identifier.getKuntaApiId()));
        return null;
      }
      
      managementAttachmentCache.put(kuntaApiAttachmentId, attachment);
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        if (!dataHash.equals(modificationHashCache.get(identifier.getKuntaApiId()))) {
          modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
          managementAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
          scaledImageStore.purgeStoredImages(kuntaApiAttachmentId);
        }
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
