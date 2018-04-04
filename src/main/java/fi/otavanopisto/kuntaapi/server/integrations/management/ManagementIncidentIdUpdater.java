package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Incident;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IncidentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationIncidentsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementIncidentIdUpdater extends IdUpdater {

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
  private OrganizationIncidentsTaskQueue organizationIncidentsTaskQueue;
  
  @Inject
  private Event<TaskRequest> taskRequest;

  @Override
  public String getName() {
    return "management-incident-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationIncidentsTaskQueue.next();
    if (task != null) {
      updateManagementIncidents(task.getOrganizationId());
    } else if (organizationIncidentsTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationIncidentsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementIncidents(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    checkRemovedManagementIncidents(api, organizationId);

    List<Incident> managementIncidents = new ArrayList<>();
    
    int page = 1;
    do {
      List<Incident> pageIncidents = listManagementIncidents(api, organizationId, page);
      managementIncidents.addAll(pageIncidents);
      if (pageIncidents.isEmpty() || pageIncidents.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementIncidents.size(); i < l; i++) {
      Incident managementIncident = managementIncidents.get(i);
      IncidentId incidentId = new IncidentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementIncident.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<IncidentId>(Operation.UPDATE, incidentId, (long) i)));
    }
  }
  
  private List<Incident> listManagementIncidents(DefaultApi api, OrganizationId organizationId, Integer page) {
    ApiResponse<List<Incident>> response = api.wpV2IncidentGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s incidents failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementIncidents(DefaultApi api, OrganizationId organizationId) {
    List<IncidentId> incidentIds = identifierController.listOrganizationIncidentIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (IncidentId incidentId : incidentIds) {
      IncidentId managementIncidentId = idController.translateIncidentId(incidentId, ManagementConsts.IDENTIFIER_NAME);
      if (managementIncidentId != null) {
        ApiResponse<Object> response = api.wpV2IncidentIdHead(managementIncidentId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the incident has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the incident should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<IncidentId>(Operation.REMOVE, incidentId)));
        }
      }
    }
  }

}
