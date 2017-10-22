package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ServiceChannelApi;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceLocationChannelInBase;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.in.KuntaApiPtvTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.in.PtvOutPtvInTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.PtvServiceChannelResolver;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class PtvServiceChannelProvider implements ServiceChannelProvider {

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
  private PtvTranslator ptvTranslator;

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
  public ServiceLocationServiceChannel updateServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId, ServiceLocationServiceChannel serviceLocationServiceChannel) {
    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(serviceLocationChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceLocationServiceChannelId != null) {
      Map<String, Object> channelData = ptvServiceChannelResolver.loadServiceChannelData(ptvServiceLocationServiceChannelId.getId());
      ServiceChannelType serviceChannelType = ptvServiceChannelResolver.resolveServiceChannelType(channelData);
      if (serviceChannelType != ServiceChannelType.SERVICE_LOCATION) {
        logger.log(Level.SEVERE, () -> String.format("Attempted to update ptv %s service channel %s into SERVICE_LOCATION", serviceChannelType, ptvServiceLocationServiceChannelId.getId()));
        return null;
      }
      
      byte[] serializeChannelData = ptvServiceChannelResolver.serializeChannelData(channelData);
      V6VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel = ptvServiceChannelResolver.unserializeServiceLocationChannel(serializeChannelData);
      V6VmOpenApiServiceLocationChannelInBase ptvServiceLocationServiceChannelIn = ptvOutPtvInTranslator.translateServiceLocationChannel(ptvServiceLocationServiceChannel);
      
      if (ptvServiceLocationServiceChannelIn == null) {
        logger.log(Level.SEVERE, () -> String.format("Failed to translate ptv %s service location service channel", ptvServiceLocationServiceChannelId.getId()));
        return null;
      }
      
      OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId == null) {
        logger.log(Level.WARNING, () -> String.format("Failed to translate ptv organization id %s into Kunta API organization id", ptvOrganizationId));
        return null;
      }
      
      ServiceChannelApi serviceChannelApi = ptvApi.getServiceChannelApi(kuntaApiOrganizationId);
      ptvServiceLocationServiceChannelIn.setServiceChannelNames(kuntaApiPtvTranslator.translateLocalizedValuesIntoLanguageItems(serviceLocationServiceChannel.getNames()));
      ptvServiceLocationServiceChannelIn.setServiceChannelDescriptions(kuntaApiPtvTranslator.translateLocalizedValuesIntoLocalizedListItems(serviceLocationServiceChannel.getDescriptions()));
      ptvServiceLocationServiceChannelIn.setPhoneNumbers(kuntaApiPtvTranslator.translatePhoneNumbers(serviceLocationServiceChannel.getPhoneNumbers()));
      ptvServiceLocationServiceChannelIn.setAddresses(kuntaApiPtvTranslator.translateAddresses(serviceLocationServiceChannel.getAddresses()));
      
      ApiResponse<V6VmOpenApiServiceLocationChannel> response = serviceChannelApi.apiV6ServiceChannelServiceLocationByIdPut(ptvServiceLocationServiceChannelId.getId(), ptvServiceLocationServiceChannelIn);
      if (response.isOk()) {
        V6VmOpenApiServiceLocationChannel ptvUpdatedServiceLocationServiceChannel = response.getResponse();
        serviceChannelTasksQueue.enqueueTask(true, new ServiceChannelUpdateTask(ptvUpdatedServiceLocationServiceChannel.getId().toString(), null));
        return ptvTranslator.translateServiceLocationServiceChannel(serviceLocationChannelId, kuntaApiOrganizationId, ptvUpdatedServiceLocationServiceChannel);
      } else {
        logger.severe(() -> String.format("Failed to update service location service channel [%d] %s", response.getStatus(), response.getMessage()));
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
  
}
