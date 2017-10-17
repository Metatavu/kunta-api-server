package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ResultType;
import fi.metatavu.ptv.client.model.V6VmOpenApiElectronicChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvClient;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ElectronicServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

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
  private PtvClient ptvClient;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;
  
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

      String path = String.format("/api/%s/ServiceChannel/%s", PtvConsts.VERSION, ptvElectronicServiceChannelId.getId());
      ApiResponse<V6VmOpenApiElectronicChannel> response = ptvClient.doGETRequest(path, new ResultType<V6VmOpenApiElectronicChannel>() {}, null, null);
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(false, new ElectronicServiceChannelRemoveTask(ptvElectronicServiceChannelId));
      }
    }
    
    if (electronicServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
