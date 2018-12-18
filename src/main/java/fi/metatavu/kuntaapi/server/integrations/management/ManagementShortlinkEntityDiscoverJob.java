package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Shortlink;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementShortlinkResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.ShortlinkIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementShortlinkEntityDiscoverJob extends EntityDiscoverJob<IdTask<ShortlinkId>> {

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
  private ManagementShortlinkResourceContainer managementShortlinkCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private ShortlinkIdTaskQueue shortlinkIdTaskQueue;

  @Override
  public String getName() {
    return "management-shortlinks";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<ShortlinkId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementShortlink(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementShortlink(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<ShortlinkId> task = shortlinkIdTaskQueue.next();
    if (task != null) {
      execute(task);
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
