package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ServiceApi;
import fi.metatavu.ptv.client.model.V7VmOpenApiService;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceInBase;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.IntegrationResponse;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.translation.KuntaApiPtvTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.translation.PtvOutPtvInTranslator;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

/**
 * PTV Service provider
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvServiceProvider implements ServiceProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private PtvServiceResourceContainer ptvServiceResourceContainer;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private PtvOutPtvInTranslator ptvOutPtvInTranslator;

  @Inject
  private KuntaApiPtvTranslator kuntaApiPtvTranslator;

  @Inject
  private PtvApi ptvApi; 
  
  @Inject
  private ServiceController serviceController;

  @Inject
  private ServiceIdTaskQueue serviceIdTaskQueue;
  
  @Override
  public Service findService(ServiceId serviceId) {
    return ptvServiceResourceContainer.get(serviceId);
  }

  @Override
  public List<Service> listServices(OrganizationId organizationId) {
    List<ServiceId> serviceIds;
    
    if (organizationId != null) {
      serviceIds = listOrganizationServiceIds(organizationId);
    } else {
      serviceIds = identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME);
    }
    
    List<Service> result = new ArrayList<>(serviceIds.size());
    for (ServiceId serviceId : serviceIds) {
      Service service = ptvServiceResourceContainer.get(serviceId);
      if (service != null) {
        result.add(service);
      }
    }
    
    return result;
  }
  
  @Override
  public IntegrationResponse<Service> updateService(ServiceId serviceId, Service service) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceId != null) {
      OrganizationId mainOrganizationId = serviceController.getServiceMainResponsibleOrganization(service);
      if (mainOrganizationId == null) {
        return IntegrationResponse.statusMessage(400, "Could not find main organization id");
      }
      
      ServiceApi serviceApi = ptvApi.getServiceApi(mainOrganizationId);
      
      ApiResponse<V7VmOpenApiService> findResponse = serviceApi.apiV7ServiceByIdGet(ptvServiceId.getId());
      if (!findResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to retrieve service [%d] %s", findResponse.getStatus(), findResponse.getMessage()));
        return IntegrationResponse.statusMessage(findResponse.getStatus(), findResponse.getMessage());
      }
      
      V7VmOpenApiServiceInBase ptvServiceIn = ptvOutPtvInTranslator.translateService(findResponse.getResponse());
      ptvServiceIn.setAreas(kuntaApiPtvTranslator.translateAreas(service.getAreas()));
      ptvServiceIn.setAreaType(service.getAreaType());
      ptvServiceIn.setFundingType(service.getFundingType());
      ptvServiceIn.setIndustrialClasses(kuntaApiPtvTranslator.translateOntologyItems(service.getIndustrialClasses()));
      ptvServiceIn.setKeywords(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(service.getKeywords()));
      ptvServiceIn.setLanguages(service.getLanguages());
      ptvServiceIn.setLegislation(kuntaApiPtvTranslator.translateLaws(service.getLegislation()));
      ptvServiceIn.setLifeEvents(kuntaApiPtvTranslator.translateOntologyItems(service.getLifeEvents()));
      ptvServiceIn.setOntologyTerms(kuntaApiPtvTranslator.translateOntologyItems(service.getOntologyTerms()));
      ptvServiceIn.setPublishingStatus(service.getPublishingStatus());
      ptvServiceIn.setRequirements(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(service.getRequirements()));
      ptvServiceIn.setServiceChargeType(service.getChargeType());
      ptvServiceIn.setServiceClasses(kuntaApiPtvTranslator.translateOntologyItems(service.getServiceClasses()));
      ptvServiceIn.setServiceDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(service.getDescriptions()));
      ptvServiceIn.setServiceNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(service.getNames()));
      ptvServiceIn.setServiceVouchers(kuntaApiPtvTranslator.translateVouchers(service.getVouchers()));
      ptvServiceIn.setServiceVouchersInUse(!service.getVouchers().isEmpty());
      ptvServiceIn.setTargetGroups(kuntaApiPtvTranslator.translateOntologyItems(service.getTargetGroups()));
      ptvServiceIn.setType(service.getType());
      ptvServiceIn.setServiceProducers(kuntaApiPtvTranslator.translateServiceProducers(service.getOrganizations()));
      ptvServiceIn.setDeleteAllIndustrialClasses(true);
      ptvServiceIn.setDeleteAllKeywords(true);
      ptvServiceIn.setDeleteAllLaws(true);
      ptvServiceIn.setDeleteAllLifeEvents(true);
      ptvServiceIn.setDeleteServiceChargeType(true);
      ptvServiceIn.setDeleteStatutoryServiceGeneralDescriptionId(false);
      
      ApiResponse<V7VmOpenApiService> updateResponse = serviceApi.apiV7ServiceByIdPut(ptvServiceId.getId(), ptvServiceIn, false);
      
      if (updateResponse.isOk()) {
        Future<Long> enqueuedTask = serviceIdTaskQueue.enqueueTask(true, new IdTask<ServiceId>(Operation.UPDATE, serviceId));
        
        try {
          enqueuedTask.get(1l, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          logger.log(Level.WARNING, "Task waiting failed, returning old version", e);
        }
        
        return findServiceAfterUpdate(serviceId);
      } else {        
        logger.severe(() -> String.format("Failed to update service [%d] %s", updateResponse.getStatus(), updateResponse.getMessage()));
        return IntegrationResponse.statusMessage(updateResponse.getStatus(), updateResponse.getMessage());
      }

    }
    
    return null;
  }

  /**
   * Returns service in new transaction. Used after the service has been updated.
   * 
   * @param serviceId serviceId
   * @return updated service 
   */
  @Transactional (TxType.REQUIRES_NEW)
  public IntegrationResponse<Service> findServiceAfterUpdate(ServiceId serviceId) {
    return IntegrationResponse.ok(findService(serviceId));
  }
  
  private List<ServiceId> listOrganizationServiceIds(OrganizationId organizationId) {
    return identifierRelationController.listServiceIdsBySourceAndParentId(PtvConsts.IDENTIFIER_NAME, organizationId);
  }
  
}
