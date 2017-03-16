package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Fragment;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationFragmentsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class ManagementFragmentIdUpdater extends IdUpdater {

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
  private OrganizationFragmentsTaskQueue organizationFragmentsTaskQueue;
  
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Override
  public String getName() {
    return "management-fragment-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationFragmentsTaskQueue.next();
    if (task != null) {
      updateManagementFragments(task.getOrganizationId());
    } else {
      organizationFragmentsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementFragments(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    checkRemovedManagementFragments(api, organizationId);

    List<Fragment> managementFragments = new ArrayList<>();
    
    int page = 1;
    do {
      List<Fragment> pageFragments = listManagementFragments(api, organizationId, page);
      managementFragments.addAll(pageFragments);
      if (pageFragments.isEmpty() || pageFragments.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementFragments.size(); i < l; i++) {
      Fragment managementFragment = managementFragments.get(i);
      FragmentId fragmentId = new FragmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementFragment.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<FragmentId>(Operation.UPDATE, fragmentId, (long) i)));
    }
  }
  
  private List<Fragment> listManagementFragments(DefaultApi api, OrganizationId organizationId, Integer page) {
    ApiResponse<List<Fragment>> response = api.wpV2FragmentGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s fragments failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementFragments(DefaultApi api, OrganizationId organizationId) {
    List<FragmentId> fragmentIds = identifierController.listOrganizationFragmentIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (FragmentId fragmentId : fragmentIds) {
      FragmentId managementFragmentId = idController.translateFragmentId(fragmentId, ManagementConsts.IDENTIFIER_NAME);
      if (managementFragmentId != null) {
        ApiResponse<Fragment> response = api.wpV2FragmentIdGet(managementFragmentId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the fragment has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the fragment should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<FragmentId>(Operation.REMOVE, fragmentId)));
        }
      }
    }
  }

}
