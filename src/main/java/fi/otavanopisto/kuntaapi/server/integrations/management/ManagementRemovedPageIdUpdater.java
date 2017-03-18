package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Page;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationPageRemovesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementRemovedPageIdUpdater extends IdUpdater {

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
  private OrganizationPageRemovesTaskQueue organizationPageRemovesTaskQueue;

  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "management-remove-page-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationPageRemovesTaskQueue.next();
    if (task != null) {
      checkRemovedManagementPages(task.getOrganizationId());
    } else {
      organizationPageRemovesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }
  
  private void checkRemovedManagementPages(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<PageId> pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (PageId pageId : pageIds) {
      PageId managementPageId = idController.translatePageId(pageId, ManagementConsts.IDENTIFIER_NAME);
      if (managementPageId != null) {
        ApiResponse<Page> response = api.wpV2PagesIdGet(managementPageId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the page has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the page should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<PageId>(Operation.REMOVE, pageId)));
        }
      }
    }
  }

}
