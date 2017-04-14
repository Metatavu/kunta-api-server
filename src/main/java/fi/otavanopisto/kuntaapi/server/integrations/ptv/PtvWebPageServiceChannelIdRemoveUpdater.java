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
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceChannels;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.WebPageServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvWebPageServiceChannelIdRemoveUpdater extends IdUpdater {

  private static final int BATCH_SIZE = 50;
  
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
  private ServiceChannelTasksQueue serviceChannelTasksQueue;

  @Resource
  private TimerService timerService;

  private int offset;

  @PostConstruct
  public void init() {
    offset = 0;
  }
  
  @Override
  public String getName() {
    return "ptv-web-page-service-channel-removed-ids";
  }
  
  @Override
  public void timeout() {
    checkRemovedIds();
  }

  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  private void checkRemovedIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    List<WebPageServiceChannelId> webPageServiceChannelIds = idController.translateIds(identifierController.listWebPageServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (WebPageServiceChannelId webPageServiceChannelId : webPageServiceChannelIds) {
      WebPageServiceChannelId ptvWebPageServiceChannelId = idController.translateWebPageServiceChannelId(webPageServiceChannelId, PtvConsts.IDENTIFIER_NAME);
      if (ptvWebPageServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate web page service channel id %s into PTV service", webPageServiceChannelId)); 
        continue;
      }
      
      ApiResponse<V4VmOpenApiServiceChannels> response = ptvApi.getServiceChannelApi().apiV4ServiceChannelByIdGet(ptvWebPageServiceChannelId.getId());
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(false, new WebPageServiceChannelRemoveTask(ptvWebPageServiceChannelId));
      }
    }
    
    if (webPageServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
