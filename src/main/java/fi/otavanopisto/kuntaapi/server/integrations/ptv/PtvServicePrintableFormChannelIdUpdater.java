package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
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
      } else if (servicePrintableFormChannelsTaskQueue.isAllowedToEnqueTasks()) {
        servicePrintableFormChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
      }
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }

  private void updateChannelIds(ServiceId kuntaApiServiceId) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
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
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, channelId);
        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
