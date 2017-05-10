package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
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

  private static final int PER_PAGE = 10;
  
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

  @Override
  public String getName() {
    return "management-remove-page-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationPageRemovesTaskQueue.next();
    if (task != null) {
      checkRemovedManagementPages(task.getOrganizationId(), task.getOffset());
    } else if (organizationPageRemovesTaskQueue.isEmpty()) {
      List<OrganizationId> kuntaApiOrganizationIds = organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL);
      for (OrganizationId kuntaApiOrganizationId : kuntaApiOrganizationIds) {
        Long pageCount = identifierController.countOrganizationPageIdsBySource(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME);
        int batchCount = (int) Math.ceil(pageCount.floatValue() / PER_PAGE);
        for (int i = 0; i < batchCount; i++) {
          organizationPageRemovesTaskQueue.enqueueTask(kuntaApiOrganizationId, i * PER_PAGE);
        }
      }
    }
  }
  
  private void checkRemovedManagementPages(OrganizationId organizationId, int offset) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<PageId> pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME, offset, PER_PAGE);
    for (PageId pageId : pageIds) {
      PageId managementPageId = idController.translatePageId(pageId, ManagementConsts.IDENTIFIER_NAME);
      if (managementPageId != null) {
        ApiResponse<Object> response = api.wpV2PagesIdHead(managementPageId.getId(), null, null, null);
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
