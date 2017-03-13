package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Shortlink;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.cache.ManagementShortlinkCache;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.ShortlinkIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class ManagementShortlinkEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ManagementShortlinkCache managementShortlinkCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private ShortlinkIdTaskQueue shortlinkIdTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-shortlinks";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }

  private void executeNextTask() {
    IdTask<ShortlinkId> task = shortlinkIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementShortlink(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteManagementShortlink(task.getId());
      }
    }
  }
  
  private void updateManagementShortlink(ShortlinkId shortlinkId, Long orderIndex) {
    OrganizationId organizationId = shortlinkId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Shortlink> response = api.wpV2ShortlinkIdGet(shortlinkId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementShortlink(organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s shortlink %s failed on [%d] %s", organizationId.getId(), shortlinkId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementShortlink(OrganizationId organizationId, Shortlink managementShortlink, Long orderIndex) {
    ShortlinkId shortlinkId = new ShortlinkId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementShortlink.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, shortlinkId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    ShortlinkId kuntaApiShortlinkId = new ShortlinkId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Shortlink shortlink = managementTranslator.translateShortlink(kuntaApiShortlinkId, managementShortlink);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(shortlink));
    managementShortlinkCache.put(kuntaApiShortlinkId, shortlink);
  }

  private void deleteManagementShortlink(ShortlinkId managementShortlinkId) {
    OrganizationId organizationId = managementShortlinkId.getOrganizationId();
    
    Identifier shortlinkIdentifier = identifierController.findIdentifierById(managementShortlinkId);
    if (shortlinkIdentifier != null) {
      ShortlinkId kuntaApiShortlinkId = new ShortlinkId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, shortlinkIdentifier.getKuntaApiId());
      modificationHashCache.clear(shortlinkIdentifier.getKuntaApiId());
      managementShortlinkCache.clear(kuntaApiShortlinkId);
      identifierController.deleteIdentifier(shortlinkIdentifier);
    }
    
  }
}
