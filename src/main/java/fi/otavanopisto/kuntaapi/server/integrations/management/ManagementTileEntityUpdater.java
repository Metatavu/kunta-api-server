package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
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
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementTileEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

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

  @PostConstruct
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      executeNextTask();
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
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

    Identifier identifier = identifierController.findIdentifierById(tileId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, tileId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }
    
    identifierRelationController.setParentId(identifier, organizationId);
    
    TileId kuntaApiTileId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Tile tile = managementTranslator.translateTile(kuntaApiTileId, managementTile);
    if (tile == null) {
      logger.severe(String.format("Could not translate management tile %s", identifier.getKuntaApiId()));
      return;
    }
    
    tileCache.put(kuntaApiTileId, tile);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(tile));
    
    if (managementTile.getFeaturedMedia() != null && managementTile.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, identifier, api, managementTile.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, Identifier tileIdentifier, DefaultApi api, Integer featuredMedia) {
    ApiResponse<Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAttachment.getId()));
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(orderIndex, managementAttachmentId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
      
      identifierRelationController.addChild(tileIdentifier, identifier);

      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, ManagementConsts.ATTACHMENT_TYPE_TILE);
      if (attachment == null) {
        logger.severe(String.format("Could not translate management attachment %s", identifier.getKuntaApiId()));
        return;
      }
      
      managementAttachmentCache.put(kuntaApiAttachmentId, attachment);
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
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
