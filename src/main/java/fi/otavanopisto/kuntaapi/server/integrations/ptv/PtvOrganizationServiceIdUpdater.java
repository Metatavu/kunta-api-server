package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.OrganizationServicesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationServiceIdUpdater extends IdUpdater {

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private OrganizationServicesTaskQueue organizationServicesTaskQueue;

  @Inject
  private Event<TaskRequest> taskRequest;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organization-services";
  }

  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationServicesTaskQueue.next();
    if (task != null) {
      updateOrganizationServiceIds(task.getOrganizationId());
    } else if (organizationServicesTaskQueue.isAllowedToEnqueTasks()) {
      organizationServicesTaskQueue.enqueueTasks(identifierController.listOrganizationsBySource(PtvConsts.IDENTIFIER_NAME));
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }

  private void updateOrganizationServiceIds(OrganizationId kuntaApiOrganizationId)  {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(kuntaApiOrganizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV organizationId", kuntaApiOrganizationId));
      return;
    }
    
    ApiResponse<List<OrganizationService>> response = ptvApi.getOrganizationServicesApi().listOrganizationOrganizationServices(ptvOrganizationId.getId(), null, null);
    if (response.isOk()) {
      List<OrganizationService> organizationServices = response.getResponse();
      for (int i = 0; i < organizationServices.size(); i++) {
        Long orderIndex = (long) i;
        OrganizationService organizationService = organizationServices.get(i);
        OrganizationServiceId organizationServiceId = new OrganizationServiceId(kuntaApiOrganizationId, PtvConsts.IDENTIFIER_NAME, organizationService.getId());
        taskRequest.fire(new TaskRequest(false, new IdTask<OrganizationServiceId>(Operation.UPDATE, organizationServiceId, orderIndex)));
      }
    } else {
      logger.warning(String.format("Organization %s services processing failed on [%d] %s", kuntaApiOrganizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
