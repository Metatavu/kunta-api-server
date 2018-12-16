package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableOrganization;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvOrganizationResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.OrganizationIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvTranslator;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V5VmOpenApiOrganizationService;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganization;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = OrganizationIdTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.NO_CONCURRENCY_POOL)
public class PtvOrganizationEntityDiscoverJob extends AbstractJmsJob<IdTask<OrganizationId>> {

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
  private PtvIdFactory ptvIdFactory;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Override
  public void execute(IdTask<OrganizationId> task) {
    OrganizationId organizationId = task.getId();
    
    if (task.getOperation() == Operation.UPDATE) {
      updateOrganization(organizationId, task.getOrderIndex());
    } else if (task.getOperation() == Operation.REMOVE) {
      logger.log(Level.SEVERE, "PTV Organization removal is not implemented");
    }
  }
  
  private void updateOrganization(OrganizationId organizationId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    if (organizationId == null) {
      return;
    }

    OrganizationId ptvOrganizationId = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate organization id %s into Ptv Id", organizationId)); 
      return;
    }
    
    ApiResponse<V8VmOpenApiOrganization> response = ptvApi.getOrganizationApi().apiV8OrganizationByIdGet(ptvOrganizationId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvOrganizationId);
      OrganizationId kuntaApiOrganizationId = kuntaApiIdFactory.createFromIdentifier(OrganizationId.class, identifier);
      V8VmOpenApiOrganization ptvOrganization = response.getResponse();
      OrganizationId kuntaApiParentOrganizationId = translateParentOrganizationId(kuntaApiOrganizationId,
          ptvOrganization);
     
      List<V5VmOpenApiOrganizationService> ptvOrganizationServices = ptvOrganization.getServices();
      if (ptvOrganizationServices == null) {
        ptvOrganizationServices = Collections.emptyList();
      }
      
      List<OrganizationService> organizationServices = new ArrayList<>(ptvOrganizationServices.size());
      for (V5VmOpenApiOrganizationService ptvOrganizationService : ptvOrganizationServices) {
        OrganizationService organizationService = translateOrganizationService(ptvOrganizationService);
        if (organizationService != null) {
          organizationServices.add(organizationService);
        }
      }
      
      fi.metatavu.kuntaapi.server.rest.model.Organization organization = ptvTranslator.translateOrganization(kuntaApiOrganizationId, kuntaApiParentOrganizationId, organizationServices, ptvOrganization);
      if (organization != null) {
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(organization));
        ptvOrganizationResourceContainer.put(kuntaApiOrganizationId, organization);
        index(identifier.getKuntaApiId(), organization, orderIndex);
      } else {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate organization %s", kuntaApiOrganizationId));
      }
    } else {
      logger.warning(() -> String.format("Organization %s processing failed on [%d] %s", ptvOrganizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private OrganizationId translateParentOrganizationId(OrganizationId kuntaApiOrganizationId, V8VmOpenApiOrganization ptvOrganization) {
    OrganizationId ptvParentOrganizationId = ptvIdFactory.createOrganizationId(ptvOrganization.getParentOrganizationId());
    OrganizationId kuntaApiParentOrganizationId = null;
    
    if (ptvParentOrganizationId != null) {
      kuntaApiParentOrganizationId = idController.translateOrganizationId(kuntaApiParentOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentOrganizationId == null) {
        logger.log(Level.INFO, () -> String.format("Could not to translate organization %s parent organization id %s into KuntaAPI id", kuntaApiOrganizationId, ptvParentOrganizationId));
      }
    }
    
    return kuntaApiParentOrganizationId;
  }
  
  private OrganizationService translateOrganizationService(V5VmOpenApiOrganizationService ptvOrganizationService) {
    if (ptvOrganizationService.getService() == null || ptvOrganizationService.getService().getId() == null || StringUtils.isBlank(ptvOrganizationService.getOrganizationId())) {
      return null;
    }
    
    UUID ptvServiceId = ptvOrganizationService.getService().getId();
    
    ServiceId kuntaApiServiceId = idController.translateServiceId(ptvIdFactory.createServiceId(ptvServiceId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiServiceId == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate service %s into Kunta API", ptvServiceId)); 
      return null;
    }
    
    OrganizationId kuntaApiServiceOrganizationId = idController.translateOrganizationId(ptvIdFactory.createOrganizationId(ptvOrganizationService.getOrganizationId()), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiServiceOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate organization %s into Kunta API", ptvOrganizationService.getOrganizationId())); 
      return null;
    }

    OrganizationService organizationService = new OrganizationService();
    organizationService.setAdditionalInformation(ptvTranslator.translateLocalizedItems(ptvOrganizationService.getAdditionalInformation()));
    organizationService.setOrganizationId(kuntaApiServiceOrganizationId.getId());
    organizationService.setProvisionType(ptvOrganizationService.getProvisionType());
    organizationService.setRoleType(ptvOrganizationService.getRoleType());
    organizationService.setServiceId(kuntaApiServiceId.getId());
    organizationService.setWebPages(ptvTranslator.translateWebPages(ptvOrganizationService.getWebPages())); 
    
    return organizationService;
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
