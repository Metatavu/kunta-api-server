package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
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
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Service;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdUpdater extends IdUpdater {

  @Inject
  private Logger logger;
  
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

  @Override
  public String getName() {
    return "ptv-service-ids";
  }
  
  @Override
  public void timeout() {
    discoverIds();
  }

  private void discoverIds() {
    List<ServiceId> existingServiceIds = idController.translateIds(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME), PtvConsts.IDENTIFIER_NAME);

    ApiResponse<List<Service>> servicesResponse = ptvApi.getServicesApi().listServices(null, null, null);
    if (!servicesResponse.isOk()) {
      logger.severe(String.format("Service list reported [%d] %s", servicesResponse.getStatus(), servicesResponse.getMessage()));
    } else {
      List<Service> services = servicesResponse.getResponse();
      for (int i = 0; i < services.size(); i++) {
        Service service = services.get(i);
        Long orderIndex = (long) i;
        ServiceId serviceId = new ServiceId(PtvConsts.IDENTIFIER_NAME, service.getId());
        existingServiceIds.remove(serviceId);
        
        boolean priority = identifierController.findIdentifierById(serviceId) == null;
        taskRequest.fire(new TaskRequest(priority, new IdTask<ServiceId>(Operation.UPDATE, serviceId, orderIndex)));
      }
    }
    
    for (ServiceId existingServiceId : existingServiceIds) {
      taskRequest.fire(new TaskRequest(false, new IdTask<ServiceId>(Operation.REMOVE, existingServiceId)));
    }
    
  }

}
