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
import fi.metatavu.restfulptv.client.model.PhoneServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PhoneServiceChannelIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvPhoneServiceChannelEntityUpdater extends EntityUpdater {

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
  private PtvPhoneServiceChannelResourceContainer ptvPhoneServiceChannelResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private PhoneServiceChannelIdTaskQueue phoneServiceChannelIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-phone-channels";
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
    IdTask<PhoneServiceChannelId> task = phoneServiceChannelIdTaskQueue.next();
    if (task != null) {
      PhoneServiceChannelId phoneServiceChannelId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel(phoneServiceChannelId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteServiceChannelChannel(phoneServiceChannelId);
      }
    }
  }

  private void updateServiceChannelChannel(PhoneServiceChannelId ptvPhoneServiceChannelId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<PhoneServiceChannel> response = ptvApi.getPhoneServiceChannelsApi().findPhoneServiceChannel(ptvPhoneServiceChannelId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPhoneServiceChannelId);
      fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel phoneServiceChannel = ptvTranslator.translatePhoneServiceChannel(response.getResponse());
      if (phoneServiceChannel != null) {
        PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, identifier);
        ptvPhoneServiceChannelResourceContainer.put(kuntaApiPhoneServiceChannelId, phoneServiceChannel);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiPhoneServiceChannelId));
      }
    } else {
      logger.warning(String.format("Phone service channel %s processing failed on [%d] %s", ptvPhoneServiceChannelId, response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteServiceChannelChannel(PhoneServiceChannelId ptvPhoneServiceChannelId) {
    Identifier phoneServiceChannelIdentifier = identifierController.findIdentifierById(ptvPhoneServiceChannelId);
    if (phoneServiceChannelIdentifier != null) {
      PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, phoneServiceChannelIdentifier);
      modificationHashCache.clear(phoneServiceChannelIdentifier.getKuntaApiId());
      ptvPhoneServiceChannelResourceContainer.clear(kuntaApiPhoneServiceChannelId);
      identifierController.deleteIdentifier(phoneServiceChannelIdentifier);      
    }
  }
  

}
