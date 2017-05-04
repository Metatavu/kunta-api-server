package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvServiceChannelIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;

  @Override
  public void timeout() {
    discoverIds();
  }
  
  public abstract ApiResponse<V3VmOpenApiGuidPage> getPage();

  public abstract Long getOrderIndex(int itemIndex, V3VmOpenApiGuidPage guidPage);
  
  public abstract void afterSuccess(V3VmOpenApiGuidPage guidPage);

  public abstract boolean getIsPriority();

  private void discoverIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<V3VmOpenApiGuidPage> response = getPage();
    if (!response.isOk()) {
      logger.severe(String.format("Service channel list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<VmOpenApiItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          VmOpenApiItem item = items.get(i);
          Long orderIndex = getOrderIndex(i, response.getResponse());
          serviceChannelTasksQueue.enqueueTask(getIsPriority(), new ServiceChannelUpdateTask(item.getId(), orderIndex));
        }
      }
      
      afterSuccess(response.getResponse());
    }
  }

}
