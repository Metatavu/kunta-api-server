package fi.metatavu.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.controllers.ServiceController;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceProvider;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.KuntaApiPtvTranslator;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvOutPtvInTranslator;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ConnectionApi;
import fi.metatavu.ptv.client.ServiceApi;
import fi.metatavu.ptv.client.model.V8VmOpenApiService;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceAndChannelRelationInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceServiceChannelInBase;

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
  private KuntaApiIdFactory kuntaApiIdFactory;

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
      
      ApiResponse<V8VmOpenApiService> findResponse = serviceApi.apiV8ServiceByIdGet(ptvServiceId.getId());
      if (!findResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to retrieve service [%d] %s", findResponse.getStatus(), findResponse.getMessage()));
        return IntegrationResponse.statusMessage(findResponse.getStatus(), findResponse.getMessage());
      }
      
      V8VmOpenApiService ptvService = findResponse.getResponse();
      
      V8VmOpenApiServiceInBase ptvServiceIn = ptvOutPtvInTranslator.translateService(ptvService);
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
      ptvServiceIn.setDeleteGeneralDescriptionId(false); 

      ApiResponse<V8VmOpenApiService> updateResponse = serviceApi.apiV8ServiceByIdPut(ptvServiceId.getId(), false, ptvServiceIn);
      
      if (updateResponse.isOk()) {
        ApiResponse<V8VmOpenApiService> updateServiceChannelsResponse = updateServiceChannels(mainOrganizationId, ptvService, service);
        if (!updateServiceChannelsResponse.isOk()) {
          logger.severe(() -> String.format("Failed to update service channels [%d] %s", updateServiceChannelsResponse.getStatus(), updateServiceChannelsResponse.getMessage()));
          return IntegrationResponse.statusMessage(updateServiceChannelsResponse.getStatus(), updateServiceChannelsResponse.getMessage());
        }
        
        serviceIdTaskQueue.enqueueTaskSync(new IdTask<ServiceId>(true, Operation.UPDATE, ptvServiceId));
        
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

  /**
   * Updates service channel connections into PTV
   * 
   * @param organizationId organization id
   * @param ptvService ptv service to be updated
   * @param service kunta api where services are updated from
   * @return response
   */
  private ApiResponse<V8VmOpenApiService> updateServiceChannels(OrganizationId organizationId, V8VmOpenApiService ptvService, Service service) {
    ConnectionApi connectionApi = ptvApi.getConnectionApi(organizationId);
    V8VmOpenApiServiceAndChannelRelationInBase relationUpdateRequest = new V8VmOpenApiServiceAndChannelRelationInBase();
    relationUpdateRequest.setDeleteAllChannelRelations(true);
    relationUpdateRequest.channelRelations(new ArrayList<V8VmOpenApiServiceServiceChannelInBase>());
    
    List<String> ptvServiceChannelIds = getPtvServiceChannelIds(service);
    
    List<V8VmOpenApiServiceServiceChannel> serviceChannels = ptvService.getServiceChannels().stream()
      .filter(channel -> channel.getServiceChannel() != null && channel.getServiceChannel().getId() != null)
      .collect(Collectors.toList());
    
    List<String> ptvExistingServiceChannelIds = serviceChannels.stream()
      .map(channel -> channel.getServiceChannel().getId().toString() )
      .collect(Collectors.collectingAndThen(Collectors.toList(), ArrayList<String>::new));
    
    for (String ptvServiceChannelId : ptvServiceChannelIds) {
      if (!ptvExistingServiceChannelIds.contains(ptvServiceChannelId)) {
        V8VmOpenApiServiceServiceChannelInBase channelRelation = new V8VmOpenApiServiceServiceChannelInBase();
        channelRelation.setServiceChannelId(ptvServiceChannelId);
        channelRelation.setDeleteAllDescriptions(false);
        channelRelation.setDeleteAllServiceHours(false);
        channelRelation.setDeleteServiceChargeType(false);
        relationUpdateRequest.addChannelRelationsItem(channelRelation);
      }
      
      ptvExistingServiceChannelIds.remove(ptvServiceChannelId);
    }
    
    serviceChannels.stream()
      .filter(serviceChannel -> !ptvExistingServiceChannelIds.contains(serviceChannel.getServiceChannel().getId().toString()))
      .map(serviceChannel -> ptvOutPtvInTranslator.translateServiceServiceChannel(serviceChannel))
      .filter(Objects::nonNull)
      .forEach(relationUpdateRequest::addChannelRelationsItem);
      
    return connectionApi.apiV8ConnectionServiceIdByServiceIdPut(ptvService.getId().toString(), relationUpdateRequest);
  }
  
  /**
   * Loads PTV service channel ids used in Kunta API service
   * 
   * @param service Kunta API Service
   * @return PTV service channel ids used in the service
   */
  private List<String> getPtvServiceChannelIds(Service service) {
    Stream<String> electronicChannelsStream = service.getElectronicServiceChannelIds().stream()
      .map(kuntaApiIdFactory::createElectronicServiceChannelId)
      .map(channelId -> idController.translateElectronicServiceChannelId(channelId, PtvConsts.IDENTIFIER_NAME))
      .filter(Objects::nonNull)
      .map(ElectronicServiceChannelId::getId);
    
    Stream<String> phoneChannelsStream = service.getPhoneServiceChannelIds().stream()
      .map(kuntaApiIdFactory::createPhoneServiceChannelId)
      .map(channelId -> idController.translatePhoneServiceChannelId(channelId, PtvConsts.IDENTIFIER_NAME))
      .filter(Objects::nonNull)
      .map(PhoneServiceChannelId::getId);

    Stream<String> printableFormChannelsStream = service.getPrintableFormServiceChannelIds().stream()
      .map(kuntaApiIdFactory::createPrintableFormServiceChannelId)
      .map(channelId -> idController.translatePrintableFormServiceChannelId(channelId, PtvConsts.IDENTIFIER_NAME))
      .filter(Objects::nonNull)
      .map(PrintableFormServiceChannelId::getId);

    Stream<String> serviceLocationChannelsStream = service.getServiceLocationServiceChannelIds().stream()
      .map(kuntaApiIdFactory::createServiceLocationServiceChannelId)
      .map(channelId -> idController.translateServiceLocationServiceChannelId(channelId, PtvConsts.IDENTIFIER_NAME))
      .filter(Objects::nonNull)
      .map(ServiceLocationServiceChannelId::getId);
    
    Stream<String> webPageChannelsStream = service.getWebPageServiceChannelIds().stream()
      .map(kuntaApiIdFactory::createWebPageServiceChannelId)
      .map(channelId -> idController.translateWebPageServiceChannelId(channelId, PtvConsts.IDENTIFIER_NAME))
      .filter(Objects::nonNull)
      .map(WebPageServiceChannelId::getId);
    
    return Stream.of(electronicChannelsStream, phoneChannelsStream, printableFormChannelsStream, serviceLocationChannelsStream, webPageChannelsStream)
      .reduce(Stream::concat)
      .orElseGet(Stream::empty)
      .collect(Collectors.toList());
  }
  
}
