package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationTilesTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.TileIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Tile;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementTileIdDiscoverJob extends IdDiscoverJob {

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
  private TileIdTaskQueue tileIdTaskQueue;
  
  @Inject
  private OrganizationTilesTaskQueue organizationTilesTaskQueue;

  @Override
  public String getName() {
    return "management-tile-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationTilesTaskQueue.next();
    if (task != null) {
      updateManagementTiles(task.getOrganizationId());
    } else if (organizationTilesTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationTilesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
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
      tileIdTaskQueue.enqueueTask(new IdTask<TileId>(false, Operation.UPDATE, tileId, (long) i));
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
        ApiResponse<Object> response = api.wpV2TileIdHead(managementTileId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the tile has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the tile should not longer be available throught API
        if (status == 404 || status == 403) {
          tileIdTaskQueue.enqueueTask(new IdTask<TileId>(false, Operation.REMOVE, tileId));
        }
      }
    }
  }
}
