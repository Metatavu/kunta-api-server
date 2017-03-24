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
import fi.metatavu.restfulptv.client.model.WebPageServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.WebPageServiceChannelIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvWebPageServiceChannelEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private PtvIdFactory ptvIdFactory;
  
  @Inject
  private PtvTranslator ptvTranslator;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private PtvWebPageServiceChannelResourceContainer ptvWebPageServiceChannelResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private WebPageServiceChannelIdTaskQueue webPageServiceChannelIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-webPage-channels";
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
    IdTask<WebPageServiceChannelId> task = webPageServiceChannelIdTaskQueue.next();
    if (task != null) {
      WebPageServiceChannelId webPageServiceChannelId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel(webPageServiceChannelId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteServiceChannelChannel(webPageServiceChannelId);
      }
    }
  }

  private void updateServiceChannelChannel(WebPageServiceChannelId ptvWebPageServiceChannelId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<WebPageServiceChannel> response = ptvApi.getWebPageServiceChannelsApi().findWebPageServiceChannel(ptvWebPageServiceChannelId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvWebPageServiceChannelId);
      WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, identifier);
      WebPageServiceChannel ptvWebPageServiceChannel = response.getResponse();
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvWebPageServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format("Could not translate organization %s into kunta api id", ptvOrganizationId));
        return;
      }
        
      fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel webPageServiceChannel = ptvTranslator.translateWebPageServiceChannel(kuntaApiWebPageServiceChannelId, kuntaApiOrganizationId, ptvWebPageServiceChannel);
      if (webPageServiceChannel != null) {
        ptvWebPageServiceChannelResourceContainer.put(kuntaApiWebPageServiceChannelId, webPageServiceChannel);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(webPageServiceChannel));
      } else {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate web page service channel %s", ptvWebPageServiceChannelId));
      }
    } else {
      logger.warning(String.format("WebPage service channel %s processing failed on [%d] %s", ptvWebPageServiceChannelId, response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteServiceChannelChannel(WebPageServiceChannelId ptvWebPageServiceChannelId) {
    Identifier webPageServiceChannelIdentifier = identifierController.findIdentifierById(ptvWebPageServiceChannelId);
    if (webPageServiceChannelIdentifier != null) {
      WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, webPageServiceChannelIdentifier);
      modificationHashCache.clear(webPageServiceChannelIdentifier.getKuntaApiId());
      ptvWebPageServiceChannelResourceContainer.clear(kuntaApiWebPageServiceChannelId);
      identifierController.deleteIdentifier(webPageServiceChannelIdentifier);      
    }
  }
  

}
