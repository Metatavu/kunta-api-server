package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.restfulptv.client.ApiResponse;
import fi.metatavu.restfulptv.client.model.ElectronicServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ElectronicServiceChannelIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvElectronicServiceChannelEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private PtvTranslator ptvTranslator;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private PtvElectronicServiceChannelResourceContainer ptvElectronicServiceChannelResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private ElectronicServiceChannelIdTaskQueue electronicServiceChannelIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-electronic-channels";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }
  
  private void executeNextTask() {
    IdTask<ElectronicServiceChannelId> task = electronicServiceChannelIdTaskQueue.next();
    if (task != null) {
      ElectronicServiceChannelId electronicServiceChannelId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel(electronicServiceChannelId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteServiceChannelChannel(electronicServiceChannelId);
      }
    }
  }

  private void updateServiceChannelChannel(ElectronicServiceChannelId ptvElectronicServiceChannelId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<ElectronicServiceChannel> response = ptvApi.getElectronicServiceChannelsApi().findElectronicServiceChannel(ptvElectronicServiceChannelId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvElectronicServiceChannelId);
      fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel electronicServiceChannel = ptvTranslator.translateElectronicServiceChannel(response.getResponse());
      if (electronicServiceChannel != null) {
        ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, identifier);
        ptvElectronicServiceChannelResourceContainer.put(kuntaApiElectronicServiceChannelId, electronicServiceChannel);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiElectronicServiceChannelId));
      }
    } else {
      logger.warning(String.format("Electronic service channel %s processing failed on [%d] %s", ptvElectronicServiceChannelId, response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteServiceChannelChannel(ElectronicServiceChannelId ptvElectronicServiceChannelId) {
    Identifier electronicServiceChannelIdentifier = identifierController.findIdentifierById(ptvElectronicServiceChannelId);
    if (electronicServiceChannelIdentifier != null) {
      ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, electronicServiceChannelIdentifier);
      modificationHashCache.clear(electronicServiceChannelIdentifier.getKuntaApiId());
      ptvElectronicServiceChannelResourceContainer.clear(kuntaApiElectronicServiceChannelId);
      identifierController.deleteIdentifier(electronicServiceChannelIdentifier);      
    }
  }
  

}
