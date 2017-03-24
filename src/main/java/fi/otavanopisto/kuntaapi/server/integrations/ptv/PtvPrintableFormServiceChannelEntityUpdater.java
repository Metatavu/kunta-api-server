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
import fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PrintableFormServiceChannelIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvPrintableFormServiceChannelEntityUpdater extends EntityUpdater {

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
  private PtvIdFactory ptvIdFactory;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private PtvPrintableFormServiceChannelResourceContainer ptvPrintableFormServiceChannelResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private PrintableFormServiceChannelIdTaskQueue printableFormServiceChannelIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-printable-form-channels";
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
    IdTask<PrintableFormServiceChannelId> task = printableFormServiceChannelIdTaskQueue.next();
    if (task != null) {
      PrintableFormServiceChannelId printableFormServiceChannelId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel(printableFormServiceChannelId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteServiceChannelChannel(printableFormServiceChannelId);
      }
    }
  }

  private void updateServiceChannelChannel(PrintableFormServiceChannelId ptvPrintableFormServiceChannelId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<PrintableFormServiceChannel> response = ptvApi.getPrintableFormServiceChannelsApi().findPrintableFormServiceChannel(ptvPrintableFormServiceChannelId.getId());
    if (response.isOk()) {
      PrintableFormServiceChannel ptvPrintableFormServiceChannel = response.getResponse();
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPrintableFormServiceChannelId);
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, identifier);
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPrintableFormServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format("Could not translate organization %s into kunta api id", ptvOrganizationId));
        return;
      }
      
      fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel printableFormServiceChannel = ptvTranslator.translatePrintableFormServiceChannel(kuntaApiPrintableFormServiceChannelId, kuntaApiOrganizationId, ptvPrintableFormServiceChannel);
      if (printableFormServiceChannel != null) {
        ptvPrintableFormServiceChannelResourceContainer.put(kuntaApiPrintableFormServiceChannelId, printableFormServiceChannel);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormServiceChannel));
      } else {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate printable form service channel %s", ptvPrintableFormServiceChannelId));
      }
      
    } else {
      logger.warning(String.format("Printable form service channel %s processing failed on [%d] %s", ptvPrintableFormServiceChannelId, response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteServiceChannelChannel(PrintableFormServiceChannelId ptvPrintableFormServiceChannelId) {
    Identifier printableFormServiceChannelIdentifier = identifierController.findIdentifierById(ptvPrintableFormServiceChannelId);
    if (printableFormServiceChannelIdentifier != null) {
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, printableFormServiceChannelIdentifier);
      modificationHashCache.clear(printableFormServiceChannelIdentifier.getKuntaApiId());
      ptvPrintableFormServiceChannelResourceContainer.clear(kuntaApiPrintableFormServiceChannelId);
      identifierController.deleteIdentifier(printableFormServiceChannelIdentifier);      
    }
  }
  

}
