package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceWebPageChannelsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.ServiceEntityUpdateTask;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.WebPageChannel ;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceWebPageChannelIdUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 10;

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private ServiceWebPageChannelsTaskQueue serviceWebPageChannelsTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "service-webpage-channels";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      ServiceEntityUpdateTask task = serviceWebPageChannelsTaskQueue.next();
      if (task != null) {
        updateChannelIds(task.getServiceId());
      } else {
        serviceWebPageChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
      }
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

  private void updateChannelIds(ServiceId kuntaApiServiceId) {
    ServiceId ptvServiceId = idController.translateServiceId(kuntaApiServiceId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV serviceId", kuntaApiServiceId));
      return;
    }

    ApiResponse<List<WebPageChannel >> response = ptvApi.getServicesApi().listServiceWebPageChannels(ptvServiceId.getId(), null, null);
    if (response.isOk()) {
      List<WebPageChannel> webPageChannels = response.getResponse();
      for (int i = 0; i < webPageChannels.size(); i++) {
        WebPageChannel webPageChannel = webPageChannels.get(i);
        WebPageChannelId channelId = new WebPageChannelId(PtvConsts.IDENTIFIER_NAME, webPageChannel.getId());
        Long orderIndex = (long) i;
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, channelId);
        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(webPageChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
