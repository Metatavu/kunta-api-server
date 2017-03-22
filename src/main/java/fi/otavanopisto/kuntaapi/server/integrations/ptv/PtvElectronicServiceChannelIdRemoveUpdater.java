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
import fi.metatavu.restfulptv.client.model.ElectronicServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvElectronicServiceChannelIdRemoveUpdater extends IdUpdater {

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
  private Event<TaskRequest> taskRequest;

  @Resource
  private TimerService timerService;

  private int offset;

  @PostConstruct
  public void init() {
    offset = 0;
  }
  
  @Override
  public String getName() {
    return "ptv-electronic-service-channel-removed-ids";
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
    
    List<ElectronicServiceChannelId> electronicServiceChannelIds = idController.translateIds(identifierController.listElectronicServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (ElectronicServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      ElectronicServiceChannelId ptvElectronicServiceChannelId = idController.translateElectronicServiceChannelId(electronicServiceChannelId, PtvConsts.IDENTIFIER_NAME);
      if (ptvElectronicServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate electronic service channel id %s into PTV service", electronicServiceChannelId)); 
        continue;
      }
      
      ApiResponse<ElectronicServiceChannel> response = ptvApi.getElectronicServiceChannelsApi().findElectronicServiceChannel(ptvElectronicServiceChannelId.getId());
      if (response.getStatus() == 404) {
        taskRequest.fire(new TaskRequest(false, new IdTask<ElectronicServiceChannelId>(Operation.REMOVE, ptvElectronicServiceChannelId))); 
      }
    }
    
    if (electronicServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}