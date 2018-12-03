package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationGuidPage;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationItem;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.OrganizationIdTaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

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

  public abstract ApiResponse<V8VmOpenApiOrganizationGuidPage> getPage();

  public abstract Long getOrderIndex(int itemIndex, V8VmOpenApiOrganizationGuidPage guidPage);
  
  public abstract void afterSuccess(V8VmOpenApiOrganizationGuidPage guidPage);

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
    
    ApiResponse<V8VmOpenApiOrganizationGuidPage> response = getPage();
    if (!response.isOk()) {
      logger.severe(() -> String.format("Organization list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<V8VmOpenApiOrganizationItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          V8VmOpenApiOrganizationItem item = items.get(i);
          Long orderIndex = getOrderIndex(i, response.getResponse());
          OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(item.getId());
          if (ptvOrganizationId != null) {
            organizationIdTaskQueue.enqueueTask(new IdTask<OrganizationId>(getIsPriority(), Operation.UPDATE, ptvOrganizationId, orderIndex));
          } else {
            logger.warning("Organization list returned item with null id");
          }
        }
      }
      
      afterSuccess(response.getResponse());      
    }
  }

}
