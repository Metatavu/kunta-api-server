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
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvClient;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.WebPageServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ResultType;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvWebPageServiceChannelIdRemoveDiscoverJob extends IdDiscoverJob {

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
    return "ptv-web-page-service-channel-removed-ids";
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
    
    List<WebPageServiceChannelId> webPageServiceChannelIds = idController.translateIds(identifierController.listWebPageServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, offset, BATCH_SIZE), PtvConsts.IDENTIFIER_NAME);
    for (WebPageServiceChannelId webPageServiceChannelId : webPageServiceChannelIds) {
      WebPageServiceChannelId ptvWebPageServiceChannelId = idController.translateWebPageServiceChannelId(webPageServiceChannelId, PtvConsts.IDENTIFIER_NAME);
      if (ptvWebPageServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Failed to translate web page service channel id %s into PTV service", webPageServiceChannelId)); 
        continue;
      }
      
      String path = String.format("/api/%s/ServiceChannel/%s", PtvConsts.VERSION, ptvWebPageServiceChannelId.getId());
      ApiResponse<V9VmOpenApiWebPageChannel> response = ptvClient.doGETRequest(null, path, new ResultType<V9VmOpenApiWebPageChannel>() {}, null, null);
      if (response.getStatus() == 404) {
        serviceChannelTasksQueue.enqueueTask(new WebPageServiceChannelRemoveTask(false, ptvWebPageServiceChannelId));
      }
    }
    
    if (webPageServiceChannelIds.size() == BATCH_SIZE) {
      offset += BATCH_SIZE;
    } else {
      offset = 0;
    }
  }

}
