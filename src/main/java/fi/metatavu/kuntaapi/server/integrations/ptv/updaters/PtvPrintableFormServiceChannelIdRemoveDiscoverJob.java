package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvClient;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.PrintableFormServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ResultType;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvPrintableFormServiceChannelIdRemoveDiscoverJob extends IdDiscoverJob {

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
      ApiResponse<V9VmOpenApiPrintableFormChannel> response = ptvClient.doGETRequest(null, path, new ResultType<V9VmOpenApiPrintableFormChannel>() {}, null, null);
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(new PrintableFormServiceChannelRemoveTask(false, ptvPrintableFormServiceChannelId));
      }
      
    }
    
    if (printableFormServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
