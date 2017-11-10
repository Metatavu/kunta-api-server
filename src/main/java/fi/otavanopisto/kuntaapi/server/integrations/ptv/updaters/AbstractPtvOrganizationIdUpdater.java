package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.VmOpenApiOrganizationGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiOrganizationItem;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractPtvOrganizationIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvIdFactory ptvIdFactory; 
  
  @Inject
  private Event<TaskRequest> taskRequest;

  public abstract ApiResponse<VmOpenApiOrganizationGuidPage> getPage();

  public abstract Long getOrderIndex(int itemIndex, VmOpenApiOrganizationGuidPage guidPage);
  
  public abstract void afterSuccess(VmOpenApiOrganizationGuidPage guidPage);

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
    
    ApiResponse<VmOpenApiOrganizationGuidPage> response = getPage();
    if (!response.isOk()) {
      logger.severe(() -> String.format("Organization list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<VmOpenApiOrganizationItem> items = response.getResponse().getItemList();
      
      if (items != null) {
        for (int i = 0; i < items.size(); i++) {
          VmOpenApiOrganizationItem item = items.get(i);
          Long orderIndex = getOrderIndex(i, response.getResponse());
          OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(item.getId());
          taskRequest.fire(new TaskRequest(getIsPriority(), new IdTask<OrganizationId>(Operation.UPDATE, ptvOrganizationId, orderIndex)));
        }
      }
      
      afterSuccess(response.getResponse());      
    }
  }

}
