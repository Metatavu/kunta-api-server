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

import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.cache.PtvOrganizationServiceCache;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.OrganizationServiceIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.restfulptv.client.ApiResponse;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationServiceEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

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
  private OrganizationServiceIdTaskQueue organizationServiceIdTaskQueue;
  
  @Inject
  private PtvOrganizationServiceCache organizationServiceCache;
  
  @Inject
  private PtvTranslator ptvTranslator;
  
  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "organization-services";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  private void executeNextTask() {
    IdTask<OrganizationServiceId> task = organizationServiceIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateOrganizationService(task.getId(), task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        logger.log(Level.SEVERE, "PTV Organization service removal is not implemented");
      }
    }
  }
  
  private void updateOrganizationService(OrganizationServiceId ptvOrganizationServiceId, Long orderIndex) {
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(ptvOrganizationServiceId.getOrganizationId(), PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into PTV organizationId", ptvOrganizationServiceId.getOrganizationId()));
      return;
    }
    
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationServiceId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into Kunta API organizationId", ptvOrganizationServiceId.getOrganizationId()));
      return;
    }
    
    ApiResponse<fi.metatavu.restfulptv.client.model.OrganizationService> response = ptvApi.getOrganizationApi().findOrganizationService(ptvOrganizationId.getId(), ptvOrganizationServiceId.getId());
    if (response.isOk()) {
      fi.metatavu.restfulptv.client.model.OrganizationService ptvOrganizationService = response.getResponse();
      ServiceId ptvServiceId = new ServiceId(PtvConsts.IDENTIFIER_NAME, ptvOrganizationService.getServiceId());
      ServiceId kuntaApiServiceId = idController.translateServiceId(ptvServiceId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiServiceId == null) {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate %s into Kunta API serviceId", ptvServiceId));
        return;
      }
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvOrganizationServiceId);
      
      OrganizationServiceId kuntaApiOrganizationServiceId = new OrganizationServiceId(kuntaApiOrganizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      OrganizationService organizationService = ptvTranslator.translateOrganizationService(kuntaApiOrganizationServiceId, kuntaApiOrganizationId, kuntaApiServiceId, ptvOrganizationService);
      identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(organizationService));
      organizationServiceCache.put(kuntaApiOrganizationServiceId, organizationService);
    } else {
      logger.warning(String.format("Organization %s processing failed on [%d] %s", ptvOrganizationServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

}
