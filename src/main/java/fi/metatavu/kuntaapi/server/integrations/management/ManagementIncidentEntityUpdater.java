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
import fi.metatavu.management.client.model.Incident;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityUpdater;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementIncidentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.IncidentIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementIncidentEntityUpdater extends EntityUpdater<IdTask<IncidentId>> {

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
  private ManagementIncidentResourceContainer managementIncidentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private IncidentIdTaskQueue incidentIdTaskQueue;

  @Override
  public String getName() {
    return "management-incidents";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<IncidentId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementIncident(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementIncident(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<IncidentId> task = incidentIdTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  private void updateManagementIncident(IncidentId incidentId, Long orderIndex) {
    OrganizationId organizationId = incidentId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Incident> response = api.wpV2IncidentIdGet(incidentId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementIncident(organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s incident %s failed on [%d] %s", organizationId.getId(), incidentId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementIncident(OrganizationId organizationId, Incident managementIncident, Long orderIndex) {
    IncidentId incidentId = new IncidentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementIncident.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, incidentId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    IncidentId kuntaApiIncidentId = new IncidentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Incident incident = managementTranslator.translateIncident(kuntaApiIncidentId, managementIncident);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(incident));
    managementIncidentCache.put(kuntaApiIncidentId, incident);
  }

  private void deleteManagementIncident(IncidentId managementIncidentId) {
    OrganizationId organizationId = managementIncidentId.getOrganizationId();
    
    Identifier incidentIdentifier = identifierController.findIdentifierById(managementIncidentId);
    if (incidentIdentifier != null) {
      IncidentId kuntaApiIncidentId = new IncidentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, incidentIdentifier.getKuntaApiId());
      modificationHashCache.clear(incidentIdentifier.getKuntaApiId());
      managementIncidentCache.clear(kuntaApiIncidentId);
      identifierController.deleteIdentifier(incidentIdentifier);
    }
    
  }
}
