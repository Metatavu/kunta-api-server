package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableOrganization;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvOrganizationResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.OrganizationIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.restfulptv.client.ApiResponse;
import fi.metatavu.restfulptv.client.model.Organization;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;
  
  @Inject
  private PtvOrganizationResourceContainer ptvOrganizationResourceContainer;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PtvTranslator ptvTranslator;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Inject
  private OrganizationIdTaskQueue organizationIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organizations";
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
    IdTask<OrganizationId> task = organizationIdTaskQueue.next();
    if (task != null) {
      OrganizationId organizationId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateOrganization(organizationId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        logger.log(Level.SEVERE, "PTV Organization removal is not implemented");
      }
    }
  }
  
  private void updateOrganization(OrganizationId organizationId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<Organization> response = ptvApi.getOrganizationApi().findOrganization(organizationId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, organizationId);
      OrganizationId kuntaApiOrganizationId = kuntaApiIdFactory.createFromIdentifier(OrganizationId.class, identifier);
      Organization ptvOrganization = response.getResponse();
      fi.metatavu.kuntaapi.server.rest.model.Organization organization = ptvTranslator.translateOrganization(kuntaApiOrganizationId, ptvOrganization);
      if (organization != null) {
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(organization));
        ptvOrganizationResourceContainer.put(kuntaApiOrganizationId, organization);
        index(identifier.getKuntaApiId(), organization, orderIndex);
      } else {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate organization %s", kuntaApiOrganizationId));
      }
    } else {
      logger.warning(String.format("Organization %s processing failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void index(String organizationId, fi.metatavu.kuntaapi.server.rest.model.Organization organization, Long orderIndex) {
    IndexableOrganization indexableOrganization = new IndexableOrganization();
    indexableOrganization.setBusinessCode(organization.getBusinessCode());
    indexableOrganization.setBusinessName(organization.getBusinessName());
    indexableOrganization.setLanguage("fi");
    indexableOrganization.setOrganizationId(organizationId);
    indexableOrganization.setOrderIndex(orderIndex);
    
    indexRequest.fire(new IndexRequest(indexableOrganization));
  }

}
