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
import fi.metatavu.ptv.client.model.V6VmOpenApiPrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvClient;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PrintableFormServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvPrintableFormServiceChannelIdRemoveUpdater extends IdUpdater {

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
    return "ptv-printable-form-service-channel-removed-ids";
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
    
    List<PrintableFormServiceChannelId> printableFormServiceChannelIds = idController.translateIds(identifierController.listPrintableFormServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (PrintableFormServiceChannelId printableFormServiceChannelId : printableFormServiceChannelIds) {
      PrintableFormServiceChannelId ptvPrintableFormServiceChannelId = idController.translatePrintableFormServiceChannelId(printableFormServiceChannelId, PtvConsts.IDENTIFIER_NAME);
      if (ptvPrintableFormServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate printable form service channel id %s into PTV service", printableFormServiceChannelId)); 
        continue;
      }
      
      String path = String.format("/api/%s/ServiceChannel/%s", PtvConsts.VERSION, ptvPrintableFormServiceChannelId.getId());
      ApiResponse<V6VmOpenApiPrintableFormChannel> response = ptvClient.doGETRequest(null, path, new ResultType<V6VmOpenApiPrintableFormChannel>() {}, null, null);
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(false, new PrintableFormServiceChannelRemoveTask(ptvPrintableFormServiceChannelId));
      }
      
    }
    
    if (printableFormServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
