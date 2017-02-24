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
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Service;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 1000 * 60 * 10;
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
  private Event<TaskRequest> taskRequest;

  private long offset;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-service-ids";
  }
  
  @Override
  public void startTimer() {
    offset = 0l;
    startTimer(WARMUP_TIME);
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void timeout(Timer timer) {
    try {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        discoverIds();
      }
    } finally {
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void discoverIds() {
    ApiResponse<List<Service>> servicesResponse = ptvApi.getServicesApi().listServices(null, offset, BATCH_SIZE);
    if (!servicesResponse.isOk()) {
      logger.severe(String.format("Service list reported [%d] %s", servicesResponse.getStatus(), servicesResponse.getMessage()));
    } else {
      List<Service> services = servicesResponse.getResponse();
      for (int i = 0; i < services.size(); i++) {
        Service service = services.get(i);
        Long orderIndex = (long) i + offset;
        ServiceId serviceId = new ServiceId(PtvConsts.IDENTIFIER_NAME, service.getId());
        boolean priority = identifierController.findIdentifierById(serviceId) == null;
        taskRequest.fire(new TaskRequest(priority, new IdTask<ServiceId>(Operation.UPDATE, serviceId, orderIndex)));
      }
      
      if (services.size() == BATCH_SIZE) {
        offset += BATCH_SIZE;
      } else {
        offset = 0;
      }
    }
  }

}
