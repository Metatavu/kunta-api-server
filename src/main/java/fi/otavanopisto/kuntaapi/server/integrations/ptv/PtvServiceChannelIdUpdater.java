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
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;

  @Resource
  private TimerService timerService;

  private Integer page;
  
  @PostConstruct
  public void init() {
    page = 0;
  }
  
  @Override
  public String getName() {
    return "service-channels";
  }
  
  @Override
  public void timeout() {
    discoverIds();
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  private void discoverIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<V3VmOpenApiGuidPage> response = ptvApi.getServiceChannelApi().apiV4ServiceChannelGet(null, page);
    if (!response.isOk()) {
      logger.severe(String.format("Service channel list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<VmOpenApiItem> items = response.getResponse().getItemList();
      
      for (int i = 0; i < items.size(); i++) {
        VmOpenApiItem item = items.get(i);
        Long orderIndex = (long) (i + (page * response.getResponse().getPageSize()));
        serviceChannelTasksQueue.enqueueTask(false, new ServiceChannelUpdateTask(item.getId(), orderIndex));
      }
      
      if (page < response.getResponse().getPageCount()) {
        page++;
      }
    }
  }

}
