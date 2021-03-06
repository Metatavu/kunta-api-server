package fi.metatavu.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.servicechannels.PtvServiceChannelResolver;
import fi.metatavu.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.KuntaApiPtvTranslator;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvOutPtvInTranslator;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ServiceChannelApi;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannelInBase;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class PtvServiceChannelProvider implements ServiceChannelProvider {

  private static final String FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL = "Failed to translate ptv %s service channel";

  private static final String FAILED_TO_UPDATE_SERVICE_CHANNEL = "Failed to update service service channel [%d] %s";

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
  public WebPageServiceChannel findWebPageServiceChannel(WebPageServiceChannelId webPageServiceChannelId) {
    return ptvWebPageServiceChannelResourceContainer.get(webPageServiceChannelId);
  }
  
  @Override
  public IntegrationResponse<ElectronicServiceChannel> updateElectronicServiceChannel(ElectronicServiceChannelId electronicChannelId, ElectronicServiceChannel electronicServiceChannel) {
    ElectronicServiceChannelId ptvElectronicServiceChannelId = idController.translateElectronicServiceChannelId(electronicChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvElectronicServiceChannelId != null) {
      V9VmOpenApiElectronicChannel ptvElectronicServiceChannel = loadServiceChannel(ServiceChannelType.ELECTRONIC_CHANNEL, ptvElectronicServiceChannelId.getId());
      if (ptvElectronicServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V9VmOpenApiElectronicChannelInBase ptvElectronicServiceChannelIn = ptvOutPtvInTranslator.translateElectronicChannel(ptvElectronicServiceChannel);
      
      if (ptvElectronicServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL, ptvElectronicServiceChannelId.getId()));
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
      ptvElectronicServiceChannelIn.setWebPage(kuntaApiPtvTranslator.translateWebPagesIntoLanguageItems(electronicServiceChannel.getWebPages()));
      ptvElectronicServiceChannelIn.setDeleteAllAttachments(true);
      ptvElectronicServiceChannelIn.setDeleteAllServiceHours(true);
      ptvElectronicServiceChannelIn.setDeleteAllSupportEmails(true);
      ptvElectronicServiceChannelIn.setDeleteAllSupportPhones(true);
      ptvElectronicServiceChannelIn.setDeleteAllWebPages(true);

      ApiResponse<V9VmOpenApiElectronicChannel> response = serviceChannelApi.apiV9ServiceChannelEChannelByIdPut(ptvElectronicServiceChannelId.getId(), ptvElectronicServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        ElectronicServiceChannel updatedServiceChannel = updateServiceChannel(updatedPtvChannelId);        
        return IntegrationResponse.ok(updatedServiceChannel);
      } else {
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }
  
  @Override
  public IntegrationResponse<PhoneServiceChannel> updatePhoneServiceChannel(PhoneServiceChannelId phoneChannelId, PhoneServiceChannel phoneServiceChannel) {
    PhoneServiceChannelId ptvPhoneServiceChannelId = idController.translatePhoneServiceChannelId(phoneChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvPhoneServiceChannelId != null) {
      V9VmOpenApiPhoneChannel ptvPhoneServiceChannel = loadServiceChannel(ServiceChannelType.PHONE, ptvPhoneServiceChannelId.getId());
      if (ptvPhoneServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V9VmOpenApiPhoneChannelInBase ptvPhoneServiceChannelIn = ptvOutPtvInTranslator.translatePhoneChannel(ptvPhoneServiceChannel);
      
      if (ptvPhoneServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL, ptvPhoneServiceChannelId.getId()));
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
      ptvPhoneServiceChannelIn.setWebPage(kuntaApiPtvTranslator.translateWebPagesIntoLanguageItems(phoneServiceChannel.getWebPages()));
      
      ApiResponse<V9VmOpenApiPhoneChannel> response = serviceChannelApi.apiV9ServiceChannelPhoneByIdPut(ptvPhoneServiceChannelId.getId(), ptvPhoneServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        PhoneServiceChannel updatedServiceChannel = updateServiceChannel(updatedPtvChannelId);     
        return IntegrationResponse.ok(updatedServiceChannel);
      } else {        
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }

  @Override
  public IntegrationResponse<PrintableFormServiceChannel> updatePrintableFormServiceChannel(PrintableFormServiceChannelId printableFormChannelId, PrintableFormServiceChannel printableFormServiceChannel) {
    PrintableFormServiceChannelId ptvPrintableFormServiceChannelId = idController.translatePrintableFormServiceChannelId(printableFormChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvPrintableFormServiceChannelId != null) {
      V9VmOpenApiPrintableFormChannel ptvPrintableFormServiceChannel = loadServiceChannel(ServiceChannelType.PRINTABLE_FORM, ptvPrintableFormServiceChannelId.getId());
      if (ptvPrintableFormServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V9VmOpenApiPrintableFormChannelInBase ptvPrintableFormServiceChannelIn = ptvOutPtvInTranslator.translatePrintableFormChannel(ptvPrintableFormServiceChannel);
      
      if (ptvPrintableFormServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL, ptvPrintableFormServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPrintableFormServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }

      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      
      ptvPrintableFormServiceChannelIn.setAreas(kuntaApiPtvTranslator.translateAreas(printableFormServiceChannel.getAreas()));
      ptvPrintableFormServiceChannelIn.setAreaType(printableFormServiceChannel.getAreaType());
      ptvPrintableFormServiceChannelIn.setAttachments(kuntaApiPtvTranslator.translateAttachments(printableFormServiceChannel.getAttachments()));
      ptvPrintableFormServiceChannelIn.setChannelUrls(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(printableFormServiceChannel.getChannelUrls()));
      ptvPrintableFormServiceChannelIn.setDeliveryAddresses(kuntaApiPtvTranslator.translateDeliveryAddresses(printableFormServiceChannel.getFormReceiver(), printableFormServiceChannel.getDeliveryAddress()));
      ptvPrintableFormServiceChannelIn.setFormIdentifier(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(printableFormServiceChannel.getFormIdentifier()));
      ptvPrintableFormServiceChannelIn.setPublishingStatus(printableFormServiceChannel.getPublishingStatus());
      ptvPrintableFormServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(printableFormServiceChannel.getDescriptions()));
      ptvPrintableFormServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(printableFormServiceChannel.getNames()));
      ptvPrintableFormServiceChannelIn.setSupportEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(printableFormServiceChannel.getSupportEmails()));
      ptvPrintableFormServiceChannelIn.setSupportPhones(kuntaApiPtvTranslator.translatePhoneNumbers(printableFormServiceChannel.getSupportPhones()));

      ApiResponse<V9VmOpenApiPrintableFormChannel> response = serviceChannelApi.apiV9ServiceChannelPrintableFormByIdPut(ptvPrintableFormServiceChannelId.getId(), ptvPrintableFormServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        PrintableFormServiceChannel updatedServiceChannel = updateServiceChannel(updatedPtvChannelId);     
        return IntegrationResponse.ok(updatedServiceChannel);
      } else {        
        logger.severe(() -> String.format("Failed to update service printable form service channel [%d] %s", response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }
  
  @Override
  public IntegrationResponse<ServiceLocationServiceChannel> updateServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId, ServiceLocationServiceChannel serviceLocationServiceChannel) {
    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(serviceLocationChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceLocationServiceChannelId != null) {
      V9VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel = loadServiceChannel(ServiceChannelType.SERVICE_LOCATION, ptvServiceLocationServiceChannelId.getId());
      if (ptvServiceLocationServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V9VmOpenApiServiceLocationChannelInBase ptvServiceLocationServiceChannelIn = ptvOutPtvInTranslator.translateServiceLocationChannel(ptvServiceLocationServiceChannel);
      
      if (ptvServiceLocationServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL, ptvServiceLocationServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      
      ptvServiceLocationServiceChannelIn.setAddresses(kuntaApiPtvTranslator.translateAddressesLocationIn(serviceLocationServiceChannel.getAddresses()));
      ptvServiceLocationServiceChannelIn.setAreas(kuntaApiPtvTranslator.translateAreas(serviceLocationServiceChannel.getAreas()));
      ptvServiceLocationServiceChannelIn.setAreaType(serviceLocationServiceChannel.getAreaType());
      ptvServiceLocationServiceChannelIn.setEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(serviceLocationServiceChannel.getEmails()));
      ptvServiceLocationServiceChannelIn.setFaxNumbers(kuntaApiPtvTranslator.translateFaxNumbers(serviceLocationServiceChannel.getPhoneNumbers()));
      ptvServiceLocationServiceChannelIn.setLanguages(serviceLocationServiceChannel.getLanguages());
      ptvServiceLocationServiceChannelIn.setPhoneNumbers(kuntaApiPtvTranslator.translatePhoneNumbers(serviceLocationServiceChannel.getPhoneNumbers()));
      ptvServiceLocationServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(serviceLocationServiceChannel.getDescriptions()));
      ptvServiceLocationServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(serviceLocationServiceChannel.getNames()));
      ptvServiceLocationServiceChannelIn.setServiceHours(kuntaApiPtvTranslator.translateServiceHours(serviceLocationServiceChannel.getServiceHours()));
      ptvServiceLocationServiceChannelIn.setWebPages(kuntaApiPtvTranslator.translateWebPages(serviceLocationServiceChannel.getWebPages()));
      
      ApiResponse<V9VmOpenApiServiceLocationChannel> response = serviceChannelApi.apiV9ServiceChannelServiceLocationByIdPut(ptvServiceLocationServiceChannelId.getId(), ptvServiceLocationServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        ServiceLocationServiceChannel updatedServiceChannel = updateServiceChannel(updatedPtvChannelId);     
        return IntegrationResponse.ok(updatedServiceChannel);
      } else {        
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
  }
  
  @Override
  public IntegrationResponse<WebPageServiceChannel> updateWebPageServiceChannel(WebPageServiceChannelId webPageChannelId, WebPageServiceChannel webPageServiceChannel) {
    WebPageServiceChannelId ptvWebPageServiceChannelId = idController.translateWebPageServiceChannelId(webPageChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvWebPageServiceChannelId != null) {
      V9VmOpenApiWebPageChannel ptvWebPageServiceChannel = loadServiceChannel(ServiceChannelType.WEB_PAGE, ptvWebPageServiceChannelId.getId());
      if (ptvWebPageServiceChannel == null) {
        return IntegrationResponse.statusMessage(Response.Status.BAD_REQUEST.getStatusCode(), COULD_NOT_RESOLVE_SERVICE_CHANNEL);
      }
      
      V9VmOpenApiWebPageChannelInBase ptvWebPageServiceChannelIn = ptvOutPtvInTranslator.translateWebPageChannel(ptvWebPageServiceChannel);
      
      if (ptvWebPageServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format(FAILED_TO_TRANSLATE_PTV_SERVICE_CHANNEL, ptvWebPageServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvWebPageServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format(FAILED_TO_TRANSLATE_PTV_ORGANIZATION_ID_INTO_KUNTA_API_ORGANIZATION_ID, ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      
      ptvWebPageServiceChannelIn.setLanguages(webPageServiceChannel.getLanguages());
      ptvWebPageServiceChannelIn.setPublishingStatus(webPageServiceChannel.getPublishingStatus());
      ptvWebPageServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(webPageServiceChannel.getDescriptions()));
      ptvWebPageServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(webPageServiceChannel.getNames()));
      ptvWebPageServiceChannelIn.setSupportEmails(kuntaApiPtvTranslator.translateEmailsIntoLanguageItems(webPageServiceChannel.getSupportEmails()));
      ptvWebPageServiceChannelIn.setSupportPhones(kuntaApiPtvTranslator.translatePhoneNumbers(webPageServiceChannel.getSupportPhones()));
      ptvWebPageServiceChannelIn.setWebPage(kuntaApiPtvTranslator.translateWebPagesIntoLanguageItems(webPageServiceChannel.getWebPages()));
      
      ApiResponse<V9VmOpenApiWebPageChannel> response = serviceChannelApi.apiV9ServiceChannelWebPageByIdPut(ptvWebPageServiceChannelId.getId(), ptvWebPageServiceChannelIn);
      if (response.isOk()) {
        String updatedPtvChannelId = response.getResponse().getId().toString();
        WebPageServiceChannel updatedServiceChannel = updateServiceChannel(updatedPtvChannelId);
        return IntegrationResponse.ok(updatedServiceChannel);
      } else {
        logger.severe(() -> String.format(FAILED_TO_UPDATE_SERVICE_CHANNEL, response.getStatus(), response.getMessage()));
        return IntegrationResponse.statusMessage(response.getStatus(), response.getMessage());
      }
      
    }
    
    return null;
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
  
  @SuppressWarnings("unchecked")
  private <T> T updateServiceChannel(String updatedPtvChannelId) {
    return (T) serviceChannelTasksQueue.enqueueTaskSync(new ServiceChannelUpdateTask(true, updatedPtvChannelId, null));
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
