package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Tile;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationTilesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class ManagementTileIdUpdater extends IdUpdater {

  private static final int PER_PAGE = 100;
  private static final int MAX_PAGES = 10;
  
  @Inject
  private Logger logger;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
 
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Inject
  private OrganizationTilesTaskQueue organizationTilesTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-tile-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationTilesTaskQueue.next();
    if (task != null) {
      updateManagementTiles(task.getOrganizationId());
    } else {
      organizationTilesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }
  
  private void updateManagementTiles(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);

    checkRemovedManagementTiles(api, organizationId);

    List<Tile> managementTiles = new ArrayList<>();
    
    int page = 1;
    do {
      List<Tile> pageTiles = listManagementTiles(api, organizationId, page);
      managementTiles.addAll(pageTiles);
      if (pageTiles.isEmpty() || pageTiles.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementTiles.size(); i < l; i++) {
      Tile managementTile = managementTiles.get(i);
      TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementTile.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<TileId>(Operation.UPDATE, tileId, (long) i)));
    }
  }
  
  private List<Tile> listManagementTiles(DefaultApi api, OrganizationId organizationId, Integer page) {
    fi.metatavu.management.client.ApiResponse<List<Tile>> response = api.wpV2TileGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s tiles failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementTiles(DefaultApi api, OrganizationId organizationId) {
    List<TileId> tileIds = identifierController.listOrganizationTileIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (TileId tileId : tileIds) {
      TileId managementTileId = idController.translateTileId(tileId, ManagementConsts.IDENTIFIER_NAME);
      if (managementTileId != null) {
        ApiResponse<Tile> response = api.wpV2TileIdGet(managementTileId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the tile has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the tile should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<TileId>(Operation.REMOVE, tileId)));
        }
      }
    }
  }
}
