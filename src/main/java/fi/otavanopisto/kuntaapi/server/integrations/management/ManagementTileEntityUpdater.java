package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Tile;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.cache.TileCache;
import fi.otavanopisto.kuntaapi.server.cache.TileImageCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.TileIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.TileIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementTileEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private TileCache tileCache;
  
  @Inject
  private TileImageCache tileImageCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<TileIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(ManagementConsts.IDENTIFIER_NAME);
  }

  @Override
  public String getName() {
    return "management-tiles";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onTileIdUpdateRequest(@Observes TileIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getOrganizationId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onTileIdRemoveRequest(@Observes TileIdRemoveRequest event) {
    if (!stopped) {
      TileId tileId = event.getId();
      
      if (!StringUtils.equals(tileId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteTile(event.getOrganizationId(), tileId);
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      TileIdUpdateRequest updateRequest = queue.next();
      if (updateRequest != null) {
        updateManagementTile(updateRequest);
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementTile(TileIdUpdateRequest updateRequest) {
    OrganizationId organizationId = updateRequest.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    TileId managementTileId = updateRequest.getId();
    Long orderIndex = updateRequest.getOrderIndex();
    
    ApiResponse<Tile> response = api.wpV2TileIdGet(managementTileId.getId(), null, null);
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
      identifierController.updateIdentifierOrderIndex(identifier, orderIndex);
    }
    
    TileId kuntaApiTileId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Tile tile = managementTranslator.translateTile(kuntaApiTileId, managementTile);
    if (tile == null) {
      logger.severe(String.format("Could not translate management tile %s", identifier.getKuntaApiId()));
      return;
    }
    
    tileCache.put(kuntaApiTileId, tile);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(tile));
    
    if (managementTile.getFeaturedMedia() != null && managementTile.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, kuntaApiTileId, api, managementTile.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, TileId kuntaApiId, DefaultApi api, Integer featuredMedia) {
    ApiResponse<Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAttachment.getId()));
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(0l, managementAttachmentId);
      }
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment);
      if (attachment == null) {
        logger.severe(String.format("Could not translate management attachment %s", identifier.getKuntaApiId()));
        return;
      }
      
      tileImageCache.put(new IdPair<>(kuntaApiId, kuntaApiAttachmentId), attachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
    }
  }
  
  private void deleteTile(OrganizationId organizationId, TileId managementTileId) {
    Identifier tileIdentifier = identifierController.findIdentifierById(managementTileId);
    if (tileIdentifier != null) {
      TileId kuntaApiTileId = new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, tileIdentifier.getKuntaApiId());
      queue.remove(managementTileId);
      modificationHashCache.clear(tileIdentifier.getKuntaApiId());
      tileCache.clear(kuntaApiTileId);
      identifierController.deleteIdentifier(tileIdentifier);
      
      List<IdPair<TileId,AttachmentId>> tileImageIds = tileImageCache.getChildIds(kuntaApiTileId);
      for (IdPair<TileId,AttachmentId> tileImageId : tileImageIds) {
        AttachmentId attachmentId = tileImageId.getChild();
        tileImageCache.clear(tileImageId);
        modificationHashCache.clear(attachmentId.getId());
        
        Identifier imageIdentifier = identifierController.findIdentifierById(attachmentId);
        if (imageIdentifier != null) {
          identifierController.deleteIdentifier(imageIdentifier);
        }
      }
    }
  }
  
  
}
