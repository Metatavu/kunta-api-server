package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceLocationChannelsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.ServiceEntityUpdateTask;
import fi.metatavu.restfulptv.client.ApiResponse;
import fi.metatavu.restfulptv.client.model.ServiceLocationChannel ;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvServiceLocationChannelIdUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private ServiceLocationChannelsTaskQueue serviceLocarionChannelsTaskQueue;

  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "service-location-channels";
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      ServiceEntityUpdateTask task = serviceLocarionChannelsTaskQueue.next();
      if (task != null) {
        updateChannelIds(task.getServiceId());
      } else {
        serviceLocarionChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
      }
    }
  }

  private void updateChannelIds(ServiceId kuntaApiServiceId) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    ServiceId ptvServiceId = idController.translateServiceId(kuntaApiServiceId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV serviceId", kuntaApiServiceId));
      return;
    }
    
    ApiResponse<List<ServiceLocationChannel>> response = ptvApi.getServicesApi().listServiceServiceLocationChannels(ptvServiceId.getId(), null, null);
    if (response.isOk()) {
      List<ServiceLocationChannel> locationChannels = response.getResponse();
      for (int i = 0; i < locationChannels.size(); i++) {
        ServiceLocationChannel locationChannel = locationChannels.get(i);
        ServiceLocationChannelId channelId = new ServiceLocationChannelId(PtvConsts.IDENTIFIER_NAME, locationChannel.getId());
        Long orderIndex = (long) i;
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, channelId);
        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(locationChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
