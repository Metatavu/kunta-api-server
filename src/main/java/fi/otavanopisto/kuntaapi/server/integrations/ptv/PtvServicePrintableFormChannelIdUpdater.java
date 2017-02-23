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
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServicePrintableFormChannelsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.ServiceEntityUpdateTask;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.PrintableFormChannel ;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServicePrintableFormChannelIdUpdater extends EntityUpdater {

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
  private ServicePrintableFormChannelsTaskQueue servicePrintableFormChannelsTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "service-printable-form-channels";
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
      ServiceEntityUpdateTask task = servicePrintableFormChannelsTaskQueue.next();
      if (task != null) {
        updateChannelIds(task.getServiceId());
      } else {
        servicePrintableFormChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
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
    
    ApiResponse<List<PrintableFormChannel >> response = ptvApi.getServicesApi().listServicePrintableFormChannels(ptvServiceId.getId(), null, null);
    if (response.isOk()) {
      List<PrintableFormChannel> printableFormChannels = response.getResponse();
      for (int i = 0; i < printableFormChannels.size(); i++) {
        PrintableFormChannel printableFormChannel = printableFormChannels.get(i);
        PrintableFormChannelId channelId = new PrintableFormChannelId(PtvConsts.IDENTIFIER_NAME, printableFormChannel.getId());
        Long orderIndex = (long) i;
        Identifier identifier = identifierController.findIdentifierById(channelId);
        if (identifier == null) {
          identifier = identifierController.createIdentifier(orderIndex, channelId);
        } else {
          identifier = identifierController.updateIdentifier(identifier, orderIndex);
        }
        
        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}