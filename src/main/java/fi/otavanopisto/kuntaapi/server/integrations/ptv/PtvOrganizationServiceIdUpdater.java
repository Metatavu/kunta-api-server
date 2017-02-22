package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
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
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationServiceIdUpdater extends IdUpdater {

  private static final int TIMER_INTERVAL = 1000 * 60 * 5;

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
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationServicesTaskQueue.next();
      if (task != null) {
        updateOrganizationServiceIds(task.getOrganizationId());
      } else {
        organizationServicesTaskQueue.enqueueTasks(identifierController.listOrganizationsBySource(PtvConsts.IDENTIFIER_NAME));
      }
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

  private void updateOrganizationServiceIds(OrganizationId kuntaApiOrganizationId)  {
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
