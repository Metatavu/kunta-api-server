package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.OrganizationServicesTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationServiceIdUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private OrganizationServicesTaskQueue organizationServicesTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organization-services";
  }

  @PostConstruct
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

  private void updateOrganizationServiceIds(OrganizationId organizationId)  {
    ApiResponse<List<OrganizationService>> response = ptvApi.getOrganizationServicesApi().listOrganizationOrganizationServices(organizationId.getId(), null, null);
    if (response.isOk()) {
      List<OrganizationService> organizationServices = response.getResponse();
      for (int i = 0; i < organizationServices.size(); i++) {
        Long orderIndex = (long) i;
        OrganizationService organizationService = organizationServices.get(i);
        OrganizationServiceId organizationServiceId = new OrganizationServiceId(organizationId, PtvConsts.IDENTIFIER_NAME, organizationService.getId());
        Identifier identifier = identifierController.findIdentifierById(organizationServiceId);
        if (identifier == null) {
          identifierController.createIdentifier(orderIndex, organizationServiceId);
        } else {
          identifierController.updateIdentifier(identifier, orderIndex);
        }
        
        identifierRelationController.setParentId(identifier, organizationId);
      }
    } else {
      logger.warning(String.format("Organization %s services processing failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
