package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
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
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Organization;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 5000;
  private static final long BATCH_SIZE = 20;
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private Event<OrganizationIdUpdateRequest> idUpdateRequest;

  private boolean stopped;
  private long offset;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organizations";
  }
  
  @Override
  public void startTimer() {
    stopped = false;
    offset = 0l;
    startTimer(WARMUP_TIME);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      try {
        if (systemSettingController.isNotTestingOrTestRunning()) {
          discoverIds();
        }
      } finally {
        startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
      }
    }
  }

  private void discoverIds() {
    ApiResponse<List<Organization>> organizationsResponse = ptvApi.getOrganizationApi().listOrganizations(offset, BATCH_SIZE);
    if (!organizationsResponse.isOk()) {
      logger.severe(String.format("Organization list reported [%d] %s", organizationsResponse.getStatus(), organizationsResponse.getMessage()));
    } else {
      List<Organization> organizations = organizationsResponse.getResponse();
      for (int i = 0; i < organizations.size(); i++) {
        Organization organization = organizations.get(i);
        Long orderIndex = (long) i + offset;
        OrganizationId organizationId = new OrganizationId(PtvConsts.IDENTIFIFER_NAME, organization.getId());
        boolean priority = identifierController.findIdentifierById(organizationId) == null;
        idUpdateRequest.fire(new OrganizationIdUpdateRequest(organizationId, orderIndex, priority));
      }
      
      if (organizations.size() == BATCH_SIZE) {
        offset += BATCH_SIZE;
      } else {
        offset = 0;
      }
    }
  }

}
