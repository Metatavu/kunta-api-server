package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

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
import fi.metatavu.ptv.client.ResultType;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvClient;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PhoneServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvPhoneServiceChannelIdRemoveUpdater extends IdUpdater {

  private static final int BATCH_SIZE = 50;
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private IdController idController;

  @Inject
  private PtvClient ptvClient;
  
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
    return "ptv-phone-service-channel-removed-ids";
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
    
    List<PhoneServiceChannelId> phoneServiceChannelIds = idController.translateIds(identifierController.listPhoneServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (PhoneServiceChannelId phoneServiceChannelId : phoneServiceChannelIds) {
      PhoneServiceChannelId ptvPhoneServiceChannelId = idController.translatePhoneServiceChannelId(phoneServiceChannelId, PtvConsts.IDENTIFIER_NAME);
      if (ptvPhoneServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate phone service channel id %s into PTV service", phoneServiceChannelId)); 
        continue;
      }
      
      String path = String.format("/api/v4/ServiceChannel/%s", ptvPhoneServiceChannelId.getId());
      ApiResponse<V4VmOpenApiPhoneChannel> response = ptvClient.doGETRequest(path, new ResultType<V4VmOpenApiPhoneChannel>() {}, null, null);
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(false, new PhoneServiceChannelRemoveTask(ptvPhoneServiceChannelId));
      }
    }
    
    if (phoneServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
