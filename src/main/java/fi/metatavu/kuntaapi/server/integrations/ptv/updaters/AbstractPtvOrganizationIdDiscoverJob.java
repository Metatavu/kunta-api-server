package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.OrganizationIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationGuidPage;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationItem;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvOrganizationIdDiscoverJob extends IdDiscoverJob {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject  
  private PtvIdFactory ptvIdFactory;  
  
  @Inject
  private OrganizationIdTaskQueue organizationIdTaskQueue;

  /**
   * Requests a guid page from PTV
   * 
   * @param page page index
   * @return response
   */
  public abstract ApiResponse<V8VmOpenApiOrganizationGuidPage> getPage(Integer page);

  /**
   * Return order index for given page and item index
   * 
   * @param page page index
   * @param itemIndex item index
   * @return  order index for given page and item index
   */
  public abstract Long getOrderIndex(Integer page, int itemIndex, V8VmOpenApiOrganizationGuidPage guidPage);
  
  public abstract void afterSuccess(V8VmOpenApiOrganizationGuidPage guidPage);

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
    
    ApiResponse<V8VmOpenApiOrganizationGuidPage> response = getPage(page);
    if (!response.isOk()) {
      logger.severe(() -> String.format("Organization list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<V8VmOpenApiOrganizationItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          V8VmOpenApiOrganizationItem item = items.get(i);
          if (item.getId() != null) {
            Long orderIndex = getOrderIndex(page, i, response.getResponse());
            OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(item.getId());
            if (ptvOrganizationId != null) {
              organizationIdTaskQueue.enqueueTask(new IdTask<OrganizationId>(getIsPriority(), Operation.UPDATE, ptvOrganizationId, orderIndex));
            } else {
              logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV id", item.getId()));
            }
          } else {
            logger.warning("Organization list returned item with null id");
          }
        }
      }
      
      afterSuccess(response.getResponse());      
    }
  }

}
