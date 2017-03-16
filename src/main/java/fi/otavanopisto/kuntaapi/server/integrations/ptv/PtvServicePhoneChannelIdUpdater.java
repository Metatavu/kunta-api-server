package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServicePhoneChannelsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.ServiceEntityUpdateTask;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.PhoneChannel ;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvServicePhoneChannelIdUpdater extends EntityUpdater {

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
  private ServicePhoneChannelsTaskQueue servicePhoneChannelsTaskQueue;

  @Override
  public String getName() {
    return "service-phone-channels";
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      ServiceEntityUpdateTask task = servicePhoneChannelsTaskQueue.next();
      if (task != null) {
        updateChannelIds(task.getServiceId());
      } else {
        servicePhoneChannelsTaskQueue.enqueueTasks(identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME));
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
    
    ApiResponse<List<PhoneChannel >> response = ptvApi.getServicesApi().listServicePhoneChannels(ptvServiceId.getId(), null, null);
    if (response.isOk()) {
      List<PhoneChannel> phoneChannels = response.getResponse();
      for (int i = 0; i < phoneChannels.size(); i++) {
        PhoneChannel phoneChannel = phoneChannels.get(i);
        Long orderIndex = (long) i;
        PhoneChannelId channelId = new PhoneChannelId(PtvConsts.IDENTIFIER_NAME, phoneChannel.getId());
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, channelId);
        identifierRelationController.setParentId(identifier, kuntaApiServiceId);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(phoneChannel));
      }
    } else {
      logger.warning(String.format("Service channel %s processing failed on [%d] %s", kuntaApiServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
