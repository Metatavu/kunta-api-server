package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ServiceChannelApi;
import fi.metatavu.ptv.client.model.V6VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannelInBase;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.IntegrationResponse;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.PtvServiceChannelResolver;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.translation.KuntaApiPtvTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.translation.PtvOutPtvInTranslator;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class PtvServiceChannelProvider implements ServiceChannelProvider {

  private static final String FAILED_TO_TRANSLATE_PTV_SERVICE_LOCATION_SERVICE_CHANNEL = "Failed to translate ptv %s service location service channel";

  private static final String FAILED_TO_UPDATE_SERVICE_LOCATION_SERVICE_CHANNEL = "Failed to update service location service channel [%d] %s";

  private static final String FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID = "Failed to translate ptv organization id %s into Kunta API organization id";

  private static final String COULD_NOT_RESOLVE_SERVICE_CHANNEL = "Could not resolve service channel";

  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private PtvElectronicServiceChannelResourceContainer ptvElectronicServiceChannelResourceContainer;
  
  @Inject
  private PtvPhoneServiceChannelResourceContainer ptvPhoneServiceChannelResourceContainer;
  
  @Inject
  private PtvPrintableFormServiceChannelResourceContainer ptvPrintableFormServiceChannelResourceContainer;
  
  @Inject
  private PtvServiceLocationServiceChannelResourceContainer ptvServiceLocationServiceChannelResourceContainer;
  
  @Inject
  private PtvWebPageServiceChannelResourceContainer ptvWebPageServiceChannelResourceContainer;

  @Inject
  private IdController idController;
  
  @Inject
  private PtvOutPtvInTranslator ptvOutPtvInTranslator;

  @Inject
  private KuntaApiPtvTranslator kuntaApiPtvTranslator;
  
  @Inject
  private PtvApi ptvApi; 

  @Inject
  private PtvIdFactory ptvIdFactory;

  @Inject
  private PtvServiceChannelResolver ptvServiceChannelResolver;
  
  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;
  
  @Override
  public ElectronicServiceChannel findElectronicServiceChannel(ElectronicServiceChannelId electronicServiceChannelId) {
    return ptvElectronicServiceChannelResourceContainer.get(electronicServiceChannelId);
  }
  
  @Override
  public PhoneServiceChannel findPhoneServiceChannel(PhoneServiceChannelId phoneServiceChannelId) {
    return ptvPhoneServiceChannelResourceContainer.get(phoneServiceChannelId);
  }
  
  @Override
  public PrintableFormServiceChannel findPrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId) {
    return ptvPrintableFormServiceChannelResourceContainer.get(printableFormServiceChannelId);
  }
  
  @Override
  public ServiceLocationServiceChannel findServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId) {
    return ptvServiceLocationServiceChannelResourceContainer.get(serviceLocationChannelId);
  }
  
  @Override
  public IntegrationResponse<ElectronicServiceChannel> updateElectronicServiceChannel(ElectronicServiceChannelId electronicChannelId, ElectronicServiceChannel electronicServiceChannel) {
    ElectronicServiceChannelId ptvElectronicServiceChannelId = idController.translateElectronicServiceChannelId(electronicChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvElectronicServiceChannelId != null) {
      V7VmOpenApiElectronicChannel ptvElectronicServiceChannel = loadServiceChannel(ServiceChannelType.ELECTRONIC_CHANNEL, ptvElectronicServiceChannelId.getId());
      if (ptvElectronicServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V6VmOpenApiElectronicChannelInBase ptvElectronicServiceChannelIn = ptvOutPtvInTranslator.translateElectronicChannel(ptvElectronicServiceChannel);
      
      if (ptvElectronicServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_LOCATION_SERVICE_CHANNEL, ptvElectronicServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvElectronicServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      
      ptvElectronicServiceChannelIn.setAreas(kuntaApiPtvTranslator.translateAreas(electronicServiceChannel.getAreas()));
      ptvElectronicServiceChannelIn.setAreaType(electronicServiceChannel.getAreaType());
      ptvElectronicServiceChannelIn.setAttachments(kuntaApiPtvTranslator.translateAttachments(electronicServiceChannel.getAttachments()));
      ptvElectronicServiceChannelIn.setPublishingStatus(electronicServiceChannel.getPublishingStatus());
      ptvElectronicServiceChannelIn.setRequiresAuthentication(electronicServiceChannel.getRequiresAuthentication());
      ptvElectronicServiceChannelIn.setRequiresSignature(electronicServiceChannel.getRequiresSignature());
      ptvElectronicServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(electronicServiceChannel.getDescriptions()));
      ptvElectronicServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(electronicServiceChannel.getNames()));
      ptvElectronicServiceChannelIn.setServiceHours(kuntaApiPtvTranslator.translateServiceHours(electronicServiceChannel.getServiceHours()));
      ptvElectronicServiceChannelIn.setSignatureQuantity(String.valueOf(electronicServiceChannel.getSignatureQuantity()));
      ptvElectronicServiceChannelIn.setSupportEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(electronicServiceChannel.getSupportEmails()));
      ptvElectronicServiceChannelIn.setSupportPhones(kuntaApiPtvTranslator.translatePhoneNumbers(electronicServiceChannel.getSupportPhones()));
      ptvElectronicServiceChannelIn.setUrls(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(electronicServiceChannel.getUrls()));
      ptvElectronicServiceChannelIn.setDeleteAllAttachments(true);
      ptvElectronicServiceChannelIn.setDeleteAllServiceHours(true);
      ptvElectronicServiceChannelIn.setDeleteAllSupportEmails(true);
      ptvElectronicServiceChannelIn.setDeleteAllSupportPhones(true);
      ptvElectronicServiceChannelIn.setDeleteAllWebPages(false);

      ApiResponse<V7VmOpenApiElectronicChannel> response = serviceChannelApi.apiV7ServiceChannelEChannelByIdPut(ptvElectronicServiceChannelId.getId(), ptvElectronicServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        waitServiceChannelUpdate(updatedPtvChannelId);
        return findElectronicChannelAfterUpdate(electronicChannelId);
      } else {        
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_LOCATION_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }
  
  @Override
  public IntegrationResponse<PhoneServiceChannel> updatePhoneServiceChannel(PhoneServiceChannelId phoneChannelId, PhoneServiceChannel phoneServiceChannel) {
    PhoneServiceChannelId ptvPhoneServiceChannelId = idController.translatePhoneServiceChannelId(phoneChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvPhoneServiceChannelId != null) {
      V7VmOpenApiPhoneChannel ptvPhoneServiceChannel = loadServiceChannel(ServiceChannelType.PHONE, ptvPhoneServiceChannelId.getId());
      if (ptvPhoneServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V7VmOpenApiPhoneChannelInBase ptvPhoneServiceChannelIn = ptvOutPtvInTranslator.translatePhoneChannel(ptvPhoneServiceChannel);
      
      if (ptvPhoneServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_LOCATION_SERVICE_CHANNEL, ptvPhoneServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPhoneServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      ptvPhoneServiceChannelIn.setAreas(kuntaApiPtvTranslator.translateAreas(phoneServiceChannel.getAreas()));
      ptvPhoneServiceChannelIn.setAreaType(phoneServiceChannel.getAreaType());
      ptvPhoneServiceChannelIn.setDeleteAllServiceHours(true);
      ptvPhoneServiceChannelIn.setDeleteAllWebPages(true);
      ptvPhoneServiceChannelIn.setIsVisibleForAll(true);
      ptvPhoneServiceChannelIn.setLanguages(phoneServiceChannel.getLanguages());
      ptvPhoneServiceChannelIn.setPhoneNumbers(kuntaApiPtvTranslator.translatePhoneNumbersWithTypes(phoneServiceChannel.getPhoneNumbers()));
      ptvPhoneServiceChannelIn.setPublishingStatus(phoneServiceChannel.getPublishingStatus());
      ptvPhoneServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(phoneServiceChannel.getDescriptions()));
      ptvPhoneServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(phoneServiceChannel.getNames()));
      ptvPhoneServiceChannelIn.setServiceHours(kuntaApiPtvTranslator.translateServiceHours(phoneServiceChannel.getServiceHours()));
      ptvPhoneServiceChannelIn.setSupportEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(phoneServiceChannel.getSupportEmails()));
      ptvPhoneServiceChannelIn.setUrls(kuntaApiPtvTranslator.translateWebPagesIntoLanguageItems(phoneServiceChannel.getWebPages()));
      
      ApiResponse<V7VmOpenApiPhoneChannel> response = serviceChannelApi.apiV7ServiceChannelPhoneByIdPut(ptvPhoneServiceChannelId.getId(), ptvPhoneServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        waitServiceChannelUpdate(updatedPtvChannelId);
        return findPhoneChannelAfterUpdate(phoneChannelId);
      } else {        
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_LOCATION_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }

  @Override
  public IntegrationResponse<ServiceLocationServiceChannel> updateServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId, ServiceLocationServiceChannel serviceLocationServiceChannel) {
    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(serviceLocationChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceLocationServiceChannelId != null) {
      V7VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel = loadServiceChannel(ServiceChannelType.SERVICE_LOCATION, ptvServiceLocationServiceChannelId.getId());
      if (ptvServiceLocationServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V7VmOpenApiServiceLocationChannelInBase ptvServiceLocationServiceChannelIn = ptvOutPtvInTranslator.translateServiceLocationChannel(ptvServiceLocationServiceChannel);
      
      if (ptvServiceLocationServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_LOCATION_SERVICE_CHANNEL, ptvServiceLocationServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      
      ptvServiceLocationServiceChannelIn.setAddresses(kuntaApiPtvTranslator.translateAddressesMovingIn(serviceLocationServiceChannel.getAddresses()));
      ptvServiceLocationServiceChannelIn.setAreas(kuntaApiPtvTranslator.translateAreas(serviceLocationServiceChannel.getAreas()));
      ptvServiceLocationServiceChannelIn.setAreaType(serviceLocationServiceChannel.getAreaType());
      ptvServiceLocationServiceChannelIn.setEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(serviceLocationServiceChannel.getEmails()));
      ptvServiceLocationServiceChannelIn.setFaxNumbers(kuntaApiPtvTranslator.translateFaxNumbers(serviceLocationServiceChannel.getPhoneNumbers()));
      ptvServiceLocationServiceChannelIn.setLanguages(serviceLocationServiceChannel.getLanguages());
      ptvServiceLocationServiceChannelIn.setPhoneNumbers(kuntaApiPtvTranslator.translatePhoneNumbers(serviceLocationServiceChannel.getPhoneNumbers()));
      ptvServiceLocationServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(serviceLocationServiceChannel.getDescriptions()));
      ptvServiceLocationServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(serviceLocationServiceChannel.getNames()));
      ptvServiceLocationServiceChannelIn.setServiceHours(kuntaApiPtvTranslator.translateServiceHours(serviceLocationServiceChannel.getServiceHours()));
      ptvServiceLocationServiceChannelIn.setWebPages(kuntaApiPtvTranslator.translateWebPagesWithOrder(serviceLocationServiceChannel.getWebPages()));
      
      ApiResponse<V7VmOpenApiServiceLocationChannel> response = serviceChannelApi.apiV7ServiceChannelServiceLocationByIdPut(ptvServiceLocationServiceChannelId.getId(), ptvServiceLocationServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        waitServiceChannelUpdate(updatedPtvChannelId);
        return findServiceLocationChannelAfterUpdate(serviceLocationChannelId);
      } else {        
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_LOCATION_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }
  
  @Override
  public WebPageServiceChannel findWebPageServiceChannelChannel(WebPageServiceChannelId webPageServiceChannelId) {
    return ptvWebPageServiceChannelResourceContainer.get(webPageServiceChannelId);
  }
  
  @Override
  public List<ElectronicServiceChannel> listElectronicServiceChannels() {
    List<ElectronicServiceChannelId> electronicServiceChannelIds = identifierController.listElectronicServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<ElectronicServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (ElectronicServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      ElectronicServiceChannel electronicServiceChannel = ptvElectronicServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<PhoneServiceChannel> listPhoneServiceChannels() {
    List<PhoneServiceChannelId> electronicServiceChannelIds = identifierController.listPhoneServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<PhoneServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (PhoneServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      PhoneServiceChannel electronicServiceChannel = ptvPhoneServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<PrintableFormServiceChannel> listPrintableFormServiceChannels() {
    List<PrintableFormServiceChannelId> electronicServiceChannelIds = identifierController.listPrintableFormServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<PrintableFormServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (PrintableFormServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      PrintableFormServiceChannel electronicServiceChannel = ptvPrintableFormServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<ServiceLocationServiceChannel> listServiceLocationServiceChannels() {
    List<ServiceLocationServiceChannelId> electronicServiceChannelIds = identifierController.listServiceLocationServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<ServiceLocationServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (ServiceLocationServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      ServiceLocationServiceChannel electronicServiceChannel = ptvServiceLocationServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<WebPageServiceChannel> listWebPageServiceChannelsChannels() {
    List<WebPageServiceChannelId> electronicServiceChannelIds = identifierController.listWebPageServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<WebPageServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (WebPageServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      WebPageServiceChannel electronicServiceChannel = ptvWebPageServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  /**
   * Returns service channel in new transaction. Used after the service channel has been updated.
   * 
   * @param serviceChannelId serviceId
   * @return updated service 
   */
  @Transactional (TxType.REQUIRES_NEW)
  public IntegrationResponse<ServiceLocationServiceChannel> findServiceLocationChannelAfterUpdate(ServiceLocationServiceChannelId serviceChannelId) {
    return IntegrationResponse.ok(findServiceLocationServiceChannel(serviceChannelId));
  }

  /**
   * Returns service channel in new transaction. Used after the service channel has been updated.
   * 
   * @param serviceChannelId serviceId
   * @return updated service 
   */
  @Transactional (TxType.REQUIRES_NEW)
  public IntegrationResponse<ElectronicServiceChannel> findElectronicChannelAfterUpdate(ElectronicServiceChannelId serviceChannelId) {
    return IntegrationResponse.ok(findElectronicServiceChannel(serviceChannelId));
  }

  /**
   * Returns service channel in new transaction. Used after the service channel has been updated.
   * 
   * @param serviceChannelId serviceId
   * @return updated service 
   */
  @Transactional (TxType.REQUIRES_NEW)
  public IntegrationResponse<PhoneServiceChannel> findPhoneChannelAfterUpdate(PhoneServiceChannelId serviceChannelId) {
    return IntegrationResponse.ok(findPhoneServiceChannel(serviceChannelId));
  }

  private void waitServiceChannelUpdate(String updatedPtvChannelId) {
    Future<Long> enqueuedTask = serviceChannelTasksQueue.enqueueTask(true, new ServiceChannelUpdateTask(updatedPtvChannelId, null));        
    try {
      if (enqueuedTask != null) {
        enqueuedTask.get(1l, TimeUnit.MINUTES);
      } else {
        logger.log(Level.SEVERE, "Task future was null, returning old version");
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      logger.log(Level.WARNING, "Task waiting failed, returning old version", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private <T> T loadServiceChannel(ServiceChannelType expectedType, String ptvServiceChannelId) {
    Map<String, Object> channelData = ptvServiceChannelResolver.loadServiceChannelData(ptvServiceChannelId);
    ServiceChannelType serviceChannelType = ptvServiceChannelResolver.resolveServiceChannelType(channelData);
    if (serviceChannelType != expectedType) {
      logger.log(Level.SEVERE, () -> String.format("Attempted to update ptv %s service channel %s into %s", serviceChannelType, ptvServiceChannelId, expectedType));
      return null;
    }
    
    byte[] serializeChannelData = ptvServiceChannelResolver.serializeChannelData(channelData);
    
    switch (expectedType) {
      case ELECTRONIC_CHANNEL:
        return (T) ptvServiceChannelResolver.unserializeElectronicChannel(serializeChannelData);
      case PHONE:
        return (T) ptvServiceChannelResolver.unserializePhoneChannel(serializeChannelData);
      case PRINTABLE_FORM:
        return (T) ptvServiceChannelResolver.unserializePrintableFormChannel(serializeChannelData);
      case SERVICE_LOCATION:
        return (T) ptvServiceChannelResolver.unserializeServiceLocationChannel(serializeChannelData);
      case WEB_PAGE:
        return (T) ptvServiceChannelResolver.unserializeWebPageChannel(serializeChannelData);
      default:
        logger.severe(() -> String.format("I don't know how to unserialize %s", expectedType));
    }
    
    return null;
  }
  
}
