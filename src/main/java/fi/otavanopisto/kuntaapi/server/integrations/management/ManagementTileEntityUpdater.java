package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
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

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Tile;

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
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
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
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateManagementTiles(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementTiles(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<Tile> managementTiles = listManagementTiles(api, organizationId);
    for (Tile managementTile : managementTiles) {
      updateManagementTile(organizationId, api, managementTile);
    }
  }

  private List<Tile> listManagementTiles(DefaultApi api, OrganizationId organizationId) {
    fi.metatavu.management.client.ApiResponse<List<Tile>> response = api.wpV2TileGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s tiles failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void updateManagementTile(OrganizationId organizationId, DefaultApi api, Tile managementTile) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementTile.getId()));

    Identifier identifier = identifierController.findIdentifierById(tileId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(tileId);
    }
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(managementTile));
    
    if (managementTile.getFeaturedMedia() != null && managementTile.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, api, managementTile.getFeaturedMedia()); 
    }
  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment attachment = response.getResponse();
      AttachmentId attachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(attachment.getId()));
      
      Identifier identifier = identifierController.findIdentifierById(attachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(attachmentId);
      }
      
      AttachmentData imageData = managementImageLoader.getImageData(attachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
      }
    }
  }

}
