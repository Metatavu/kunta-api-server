package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
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
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdUpdater extends IdUpdater {

  private static final long BATCH_SIZE = 50;
  
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

  @Resource
  private TimerService timerService;

  private long offset;

  @PostConstruct
  public void init() {
    offset = 0;
  }
  
  @Override
  public String getName() {
    return "ptv-service-ids";
  }
  
  @Override
  public void timeout() {
    discoverIds();
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  private void discoverIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }

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
