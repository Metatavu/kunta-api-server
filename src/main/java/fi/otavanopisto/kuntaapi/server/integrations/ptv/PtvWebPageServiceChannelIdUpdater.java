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
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.restfulptv.client.ApiResponse;
import fi.metatavu.restfulptv.client.model.WebPageServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvWebPageServiceChannelIdUpdater extends IdUpdater {

  private static final long BATCH_SIZE = 20;
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject  
  private PtvIdFactory ptvIdFactory;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private Event<TaskRequest> taskRequest;

  @Resource
  private TimerService timerService;

  private long offset;
  
  @PostConstruct
  public void init() {
    offset = 0;
  }
  
  @Override
  public String getName() {
    return "web-page-service-channels";
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
    
    ApiResponse<List<WebPageServiceChannel>> response = ptvApi.getWebPageServiceChannelsApi().listWebPageServiceChannels(offset, BATCH_SIZE);
    if (!response.isOk()) {
      logger.severe(String.format("Web page service channel list reported [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      List<WebPageServiceChannel> webPageServiceChannels = response.getResponse();
      for (int i = 0; i < webPageServiceChannels.size(); i++) {
        WebPageServiceChannel webPageServiceChannel = webPageServiceChannels.get(i);
        Long orderIndex = (long) i + offset;
        WebPageServiceChannelId webPageServiceChannelId = ptvIdFactory.createWebPageServiceChannelId(webPageServiceChannel.getId());
        boolean priority = identifierController.findIdentifierById(webPageServiceChannelId) == null;
        taskRequest.fire(new TaskRequest(priority, new IdTask<WebPageServiceChannelId>(Operation.UPDATE, webPageServiceChannelId, orderIndex)));
      }
      
      if (webPageServiceChannels.size() == BATCH_SIZE) {
        offset += BATCH_SIZE;
      } else {
        offset = 0;
      }
    }
  }

}
