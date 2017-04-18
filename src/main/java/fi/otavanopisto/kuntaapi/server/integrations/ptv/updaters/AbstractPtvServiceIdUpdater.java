package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvServiceIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvIdFactory ptvIdFactory;
  
  @Inject
  private Event<TaskRequest> taskRequest;

  public abstract ApiResponse<V3VmOpenApiGuidPage> getPage();

  public abstract Long getOrderIndex(int itemIndex, V3VmOpenApiGuidPage guidPage);
  
  public abstract void afterSuccess(V3VmOpenApiGuidPage guidPage);

  public abstract boolean getIsPriority();
  
  @Override
  public void timeout() {
    discoverIds();
  }
  
  private void discoverIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    ApiResponse<V3VmOpenApiGuidPage> response = getPage();
    if (!response.isOk()) {
      logger.severe(String.format("Organization list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<VmOpenApiItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          VmOpenApiItem item = items.get(i);
          Long orderIndex = getOrderIndex(i, response.getResponse());
          ServiceId ptvServiceId = ptvIdFactory.createServiceId(item.getId());
          taskRequest.fire(new TaskRequest(getIsPriority(), new IdTask<ServiceId>(Operation.UPDATE, ptvServiceId, orderIndex)));
        }
      }
      
      afterSuccess(response.getResponse());
    }
    
  }

}
