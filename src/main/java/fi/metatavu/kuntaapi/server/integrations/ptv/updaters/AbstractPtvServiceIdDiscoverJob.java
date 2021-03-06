package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiItem;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvServiceIdDiscoverJob extends IdDiscoverJob {
  
  private static final int DELIVERY_INTERVAL = 1000;

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvIdFactory ptvIdFactory;
  
  @Inject
  private ServiceIdTaskQueue serviceIdTaskQueue;
  
  /**
   * Returns system setting for enabling / disabling discover job
   * 
   * @return system setting for enabling / disabling discover job
   */
  public abstract String getEnabledSettingKey();
  
  /**
   * Requests a guid page from PTV
   * 
   * @param page page index
   * @return response
   */
  public abstract ApiResponse<V3VmOpenApiGuidPage> getPage(Integer page);

  /**
   * Return order index for given page and item index
   * 
   * @param page page index
   * @param itemIndex item index
   * @return  order index for given page and item index
   */
  public abstract Long getOrderIndex(Integer page, int itemIndex, V3VmOpenApiGuidPage guidPage);
  
  public abstract void afterSuccess(V3VmOpenApiGuidPage guidPage);

  public abstract boolean getIsPriority();
  
  /**
   * Performs id discovery for given page
   * 
   * @param page page index
   */
  protected void discoverIds(Integer page) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }

    if ("false".equals(systemSettingController.getSettingValue(getEnabledSettingKey(), "true"))) {
      return;
    }
    
    ApiResponse<V3VmOpenApiGuidPage> response = getPage(page);
    if (!response.isOk()) {
      logger.severe(String.format("Organization list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<VmOpenApiItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          VmOpenApiItem item = items.get(i);
          Long orderIndex = getOrderIndex(page, i, response.getResponse());
          ServiceId ptvServiceId = ptvIdFactory.createServiceId(item.getId());
          serviceIdTaskQueue.enqueueTask(new IdTask<ServiceId>(getIsPriority(), Operation.UPDATE, ptvServiceId, orderIndex), getDeliveryDelay(i));
        }
      }
      
      afterSuccess(response.getResponse());
    }
    
  }

  /**
   * Returns task queue delay time in milliseconds
   * 
   * @param index index
   * @return task queue delay time in milliseconds
   */
  private int getDeliveryDelay(int index) {
    if (getIsPriority()) {
      return 0;
    }
    
    if (systemSettingController.inTestMode()) {
      return 10 * index;
    }
    
    return index * DELIVERY_INTERVAL;
  }
}
