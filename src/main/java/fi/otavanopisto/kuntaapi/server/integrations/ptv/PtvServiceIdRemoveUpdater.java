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

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V4VmOpenApiService;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdRemoveUpdater extends IdUpdater {

  private static final long BATCH_SIZE = 50;
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private IdController idController;

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
    return "ptv-service-removed-ids";
  }
  
  @Override
  public void timeout() {
    checkRemovedIds();
  }

  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  private void checkRemovedIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }

    List<ServiceId> existingServiceIds = idController.translateIds(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (ServiceId existingServiceId : existingServiceIds) {
      ServiceId ptvServiceId = idController.translateServiceId(existingServiceId, PtvConsts.IDENTIFIER_NAME);
      if (ptvServiceId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate service %s into PTV service", existingServiceId)); 
        continue;
      }
      
      ApiResponse<V4VmOpenApiService> response = ptvApi.getServiceApi().apiV4ServiceByIdGet(ptvServiceId.getId());
      if (response.getStatus() == 404) {
        taskRequest.fire(new TaskRequest(false, new IdTask<ServiceId>(Operation.REMOVE, ptvServiceId)));
      }
    }
    
    if (existingServiceIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
