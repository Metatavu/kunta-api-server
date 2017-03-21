package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServicePrintableFormChannelsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.ServiceEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServicePrintableFormChannelIdUpdater extends EntityUpdater {

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
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      ServiceEntityUpdateTask task = servicePrintableFormChannelsTaskQueue.next();
      if (task != null) {
        updateChannelIds(task.getServiceId());
      } else if (servicePrintableFormChannelsTaskQueue.isEmpty()) {
        servicePrintableFormChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
      }
    }
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  private void updateChannelIds(ServiceId kuntaApiServiceId) {
    // TODO: FIXME!
//    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
//      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
//      return;
//    }
//    
//    ServiceId ptvServiceId = idController.translateServiceId(kuntaApiServiceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV serviceId", kuntaApiServiceId));
//      return;
//    }
//    
//    ApiResponse<List<PrintableFormChannel >> response = ptvApi.getServicesApi().listServicePrintableFormChannels(ptvServiceId.getId(), null, null);
//    if (response.isOk()) {
//      List<PrintableFormChannel> printableFormChannels = response.getResponse();
//      for (int i = 0; i < printableFormChannels.size(); i++) {
//        PrintableFormChannel printableFormChannel = printableFormChannels.get(i);
//        PrintableFormServiceChannelId channelId = new PrintableFormServiceChannelId(PtvConsts.IDENTIFIER_NAME, printableFormChannel.getId());
//        Long orderIndex = (long) i;
//        Identifier identifier = identifierController.acquireIdentifier(orderIndex, channelId);
//        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
//        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormChannel));
//      }
//    } else {
//      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
//    }
  }

}
