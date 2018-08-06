package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvServiceIdDiscoverJob extends IdDiscoverJob {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvIdFactory ptvIdFactory;
  
  @Inject
  private ServiceIdTaskQueue serviceIdTaskQueue;

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
          serviceIdTaskQueue.enqueueTask(new IdTask<ServiceId>(getIsPriority(), Operation.UPDATE, ptvServiceId, orderIndex));
        }
      }
      
      afterSuccess(response.getResponse());
    }
    
  }

}