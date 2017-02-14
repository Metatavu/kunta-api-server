package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.ServiceIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.PrintableFormChannel ;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ServicePrintableFormChannelIdUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<ServiceId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "service-printable-form-channels";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }

  public void onServiceIdUpdateRequest(@Observes ServiceIdUpdateRequest event) {
    if (!stopped) {
      if (!PtvConsts.IDENTIFIFER_NAME.equals(event.getId().getSource())) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event.getId());
        queue.add(0, event.getId());
      } else {
        if (!queue.contains(event.getId())) {
          queue.add(event.getId());
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning() && !queue.isEmpty()) {
        updateChannelIds(queue.remove(0));
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateChannelIds(ServiceId serviceId) {
    ApiResponse<List<PrintableFormChannel >> response = ptvApi.getServicesApi().listServicePrintableFormChannels(serviceId.getId(), null, null);
    if (response.isOk()) {
      List<PrintableFormChannel> printableFormChannels = response.getResponse();
      for (int i = 0; i < printableFormChannels.size(); i++) {
        PrintableFormChannel printableFormChannel = printableFormChannels.get(i);
        PrintableFormChannelId channelId = new PrintableFormChannelId(PtvConsts.IDENTIFIFER_NAME, printableFormChannel.getId());
        Long orderIndex = (long) i;
        Identifier identifier = identifierController.findIdentifierById(channelId);
        if (identifier == null) {
          identifier = identifierController.createIdentifier(orderIndex, channelId);
        } else {
          identifier = identifierController.updateIdentifier(identifier, orderIndex);
        }
        
        identifierRelationController.setParentId(identifier, serviceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", serviceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
