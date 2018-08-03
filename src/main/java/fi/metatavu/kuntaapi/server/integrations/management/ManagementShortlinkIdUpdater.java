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
import fi.metatavu.kuntaapi.server.discover.IdUpdater;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationShortlinksTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.ShortlinkIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Shortlink;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementShortlinkIdUpdater extends IdUpdater {

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
  private OrganizationShortlinksTaskQueue organizationShortlinksTaskQueue;

  @Inject
  private ShortlinkIdTaskQueue shortlinkIdTaskQueue;
  
  @Override
  public String getName() {
    return "management-shortlink-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationShortlinksTaskQueue.next();
    if (task != null) {
      updateManagementShortlinks(task.getOrganizationId());
    } else if (organizationShortlinksTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationShortlinksTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementShortlinks(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    checkRemovedManagementShortlinks(api, organizationId);

    List<Shortlink> managementShortlinks = new ArrayList<>();
    
    int page = 1;
    do {
      List<Shortlink> pageShortlinks = listManagementShortlinks(api, organizationId, page);
      managementShortlinks.addAll(pageShortlinks);
      if (pageShortlinks.isEmpty() || pageShortlinks.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementShortlinks.size(); i < l; i++) {
      Shortlink managementShortlink = managementShortlinks.get(i);
      ShortlinkId shortlinkId = new ShortlinkId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementShortlink.getId()));
      shortlinkIdTaskQueue.enqueueTask(new IdTask<ShortlinkId>(false, Operation.UPDATE, shortlinkId, (long) i));
    }
  }
  
  private List<Shortlink> listManagementShortlinks(DefaultApi api, OrganizationId organizationId, Integer page) {
    ApiResponse<List<Shortlink>> response = api.wpV2ShortlinkGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s shortlinks failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementShortlinks(DefaultApi api, OrganizationId organizationId) {
    List<ShortlinkId> shortlinkIds = identifierController.listOrganizationShortlinkIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (ShortlinkId shortlinkId : shortlinkIds) {
      ShortlinkId managementShortlinkId = idController.translateShortlinkId(shortlinkId, ManagementConsts.IDENTIFIER_NAME);
      if (managementShortlinkId != null) {
        ApiResponse<Object> response = api.wpV2ShortlinkIdHead(managementShortlinkId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the shortlink has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the shortlink should not longer be available throught API
        if (status == 404 || status == 403) {
          shortlinkIdTaskQueue.enqueueTask(new IdTask<ShortlinkId>(false, Operation.REMOVE, shortlinkId));
        }
      }
    }
  }

}
