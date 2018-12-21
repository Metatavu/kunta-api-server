package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V9VmOpenApiService;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdRemoveDiscoverJob extends IdDiscoverJob {

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
  private ServiceIdTaskQueue serviceIdTaskQueue;

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
      
      ApiResponse<V9VmOpenApiService> response = ptvApi.getServiceApi(null).apiV9ServiceByIdGet(ptvServiceId.getId());
      if (response.getStatus() == 404) {
        serviceIdTaskQueue.enqueueTask(new IdTask<ServiceId>(false, Operation.REMOVE, ptvServiceId));
      }
    }
    
    if (existingServiceIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
