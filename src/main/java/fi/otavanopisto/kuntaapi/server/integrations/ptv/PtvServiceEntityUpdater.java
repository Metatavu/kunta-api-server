package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.ServiceIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Service;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvServiceEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<ServiceId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "services";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }

  public void onServiceIdUpdateRequest(@Observes ServiceIdUpdateRequest event) {
    if (!stopped) {
      if (event.isPriority()) {
        queue.remove(event.getId());
        queue.add(0, event.getId());
      } else {
        if (!queue.contains(event.getId())) {
          queue.add(event.getId());
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        ServiceId serviceId = queue.iterator().next();
        if (PtvConsts.IDENTIFIFER_NAME.equals(serviceId.getSource())) {
          ApiResponse<Service> response = ptvApi.getServicesApi().findService(serviceId.getId());
          if (response.isOk()) {
            Identifier identifier = identifierController.findIdentifierById(serviceId);
            if (identifier == null) {
              identifierController.createIdentifier(serviceId);
            }
          } else {
            logger.warning(String.format("Service %s processing failed on [%d] %s", serviceId.getId(), response.getStatus(), response.getMessage()));
          }          
        }        
      }

      startTimer(TIMER_INTERVAL);
    }
  }

}
