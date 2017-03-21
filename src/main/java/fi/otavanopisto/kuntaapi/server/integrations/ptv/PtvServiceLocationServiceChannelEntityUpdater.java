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
import fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceLocationServiceChannelIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceLocationServiceChannelEntityUpdater extends EntityUpdater {

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
  private PtvServiceLocationServiceChannelResourceContainer ptvServiceLocationServiceChannelResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private ServiceLocationServiceChannelIdTaskQueue serviceLocationServiceChannelIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-service-location-channels";
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
    IdTask<ServiceLocationServiceChannelId> task = serviceLocationServiceChannelIdTaskQueue.next();
    if (task != null) {
      ServiceLocationServiceChannelId serviceLocationServiceChannelId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel(serviceLocationServiceChannelId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteServiceChannelChannel(serviceLocationServiceChannelId);
      }
    }
  }

  private void updateServiceChannelChannel(ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<ServiceLocationServiceChannel> response = ptvApi.getServiceLocationServiceChannelsApi().findServiceLocationServiceChannel(ptvServiceLocationServiceChannelId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvServiceLocationServiceChannelId);
      fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel serviceLocationServiceChannel = ptvTranslator.translateServiceLocationServiceChannel(response.getResponse());
      if (serviceLocationServiceChannel != null) {
        ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, identifier);
        ptvServiceLocationServiceChannelResourceContainer.put(kuntaApiServiceLocationServiceChannelId, serviceLocationServiceChannel);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiServiceLocationServiceChannelId));
      }
    } else {
      logger.warning(String.format("Service location service channel %s processing failed on [%d] %s", ptvServiceLocationServiceChannelId, response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteServiceChannelChannel(ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId) {
    Identifier serviceLocationServiceChannelIdentifier = identifierController.findIdentifierById(ptvServiceLocationServiceChannelId);
    if (serviceLocationServiceChannelIdentifier != null) {
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, serviceLocationServiceChannelIdentifier);
      modificationHashCache.clear(serviceLocationServiceChannelIdentifier.getKuntaApiId());
      ptvServiceLocationServiceChannelResourceContainer.clear(kuntaApiServiceLocationServiceChannelId);
      identifierController.deleteIdentifier(serviceLocationServiceChannelIdentifier);      
    }
  }
  

}
