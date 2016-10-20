package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;
import fi.otavanopisto.restfulptv.client.ApiResponse;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@RequestScoped
public class PtvServiceChannelProvider extends AbstractPtvProvider implements ServiceChannelProvider {

  private static final String SERVICE_TRANSLATE_FAILURE = "Failed to translate service id %s into PTV id";
  private static final String ELECTRONIC_SERVICE_CHANNEL_TRANSLATE_FAILURE = "Failed to translate electronic service channel %s into PTV id";
  private static final String PHONE_SERVICE_CHANNEL_TRANSLATE_FAILURE = "Failed to translate phone service channel %s into PTV id";
  private static final String PRINTABLE_FORM_SERVICE_CHANNEL_TRANSLATE_FAILURE = "Failed to translate printable form service channel %s into PTV id";
  private static final String SERVICE_LOCATION_SERVICE_CHANNEL_TRANSLATE_FAILURE = "Failed to translate service location service channel %s into PTV id";
  private static final String WEBPAGE_SERVICE_CHANNEL_TRANSLATE_FAILURE = "Failed to translate webpage service channel %s into PTV id";

  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  @Override
  public ElectronicChannel findElectronicChannel(ServiceId serviceId, ElectronicServiceChannelId electronicServiceChannelId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ElectronicServiceChannelId ptvElectronicServiceChannelId = idController.translateElectronicServiceChannelId(electronicServiceChannelId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvElectronicServiceChannelId == null) {
      logger.severe(String.format(ELECTRONIC_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.ElectronicChannel> electronicChannelResponse = ptvApi.getElectronicChannelsApi().findServiceElectronicChannel(ptvServiceId.getId(), ptvElectronicServiceChannelId.getId());
    if (!electronicChannelResponse.isOk()) {
      logger.severe(String.format("Electronic channels list of service %s reported [%d] %s", serviceId.toString(), electronicChannelResponse.getStatus(), electronicChannelResponse.getMessage()));
      return null;
    } else {
      return translateElectronicChannel(electronicChannelResponse.getResponse());
    }
  }
  
  @Override
  public PhoneChannel findPhoneChannel(ServiceId serviceId, PhoneChannelId phoneChannelId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    PhoneChannelId ptvPhoneChannelId = idController.translatePhoneServiceChannelId(phoneChannelId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvPhoneChannelId == null) {
      logger.severe(String.format(PHONE_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.PhoneChannel> phoneChannelResponse = ptvApi.getPhoneChannelsApi().findServicePhoneChannel(ptvServiceId.getId(), ptvPhoneChannelId.getId());
    if (!phoneChannelResponse.isOk()) {
      logger.severe(String.format("Phone channels list of service %s reported [%d] %s", serviceId.toString(), phoneChannelResponse.getStatus(), phoneChannelResponse.getMessage()));
      return null;
    } else {
      return translatePhoneChannel(phoneChannelResponse.getResponse());
    }
  }
  
  @Override
  public PrintableFormChannel findPrintableFormChannel(ServiceId serviceId, PrintableFormChannelId printableFormChannelId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    PrintableFormChannelId ptvPrintableFormChannelId = idController.translatePrintableFormServiceChannelId(printableFormChannelId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvPrintableFormChannelId == null) {
      logger.severe(String.format(PRINTABLE_FORM_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.PrintableFormChannel> printableFormChannelResponse = ptvApi.getPrintableFormChannelsApi().findServicePrintableFormChannel(ptvServiceId.getId(), ptvPrintableFormChannelId.getId());
    if (!printableFormChannelResponse.isOk()) {
      logger.severe(String.format("Printable form channels list of service %s reported [%d] %s", serviceId.toString(), printableFormChannelResponse.getStatus(), printableFormChannelResponse.getMessage()));
      return null;
    } else {
      return translatePrintableFormChannel(printableFormChannelResponse.getResponse());
    }
  }
  
  @Override
  public ServiceLocationChannel findServiceLocationChannel(ServiceId serviceId, ServiceLocationChannelId serviceLocationChannelId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ServiceLocationChannelId ptvServiceLocationChannelId = idController.translateServiceLocationChannelId(serviceLocationChannelId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceLocationChannelId == null) {
      logger.severe(String.format(SERVICE_LOCATION_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel> serviceLocationChannelResponse = ptvApi.getServiceLocationChannelsApi().findServiceServiceLocationChannel(ptvServiceId.getId(), ptvServiceLocationChannelId.getId());
    if (!serviceLocationChannelResponse.isOk()) {
      logger.severe(String.format("Service location channels list of service %s reported [%d] %s", serviceId.toString(), serviceLocationChannelResponse.getStatus(), serviceLocationChannelResponse.getMessage()));
      return null;
    } else {
      return translateServiceLocationChannel(serviceLocationChannelResponse.getResponse());
    }
  }
  
  @Override
  public WebPageChannel findWebPageChannelChannel(ServiceId serviceId, WebPageChannelId webPageChannelId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    WebPageChannelId ptvWebPageChannelId = idController.translateWebPageServiceChannelId(webPageChannelId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvWebPageChannelId == null) {
      logger.severe(String.format(WEBPAGE_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.WebPageChannel> webPageChannelResponse = ptvApi.getWebPageChannelsApi().findServiceWebPageChannel(ptvServiceId.getId(), ptvWebPageChannelId.getId());
    if (!webPageChannelResponse.isOk()) {
      logger.severe(String.format("Web page channels list of service %s reported [%d] %s", serviceId.toString(), webPageChannelResponse.getStatus(), webPageChannelResponse.getMessage()));
      return null;
    } else {
      return translateWebPageChannel(webPageChannelResponse.getResponse());
    }
  }
  
  @Override
  public List<ElectronicChannel> listElectronicChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.ElectronicChannel>> electronicChannelsResponse = ptvApi.getElectronicChannelsApi().listServiceElectronicChannels(ptvServiceId.getId(), null, null);
    if (!electronicChannelsResponse.isOk()) {
      logger.severe(String.format("Electronic channels list of service %s reported [%d] %s", serviceId.toString(), electronicChannelsResponse.getStatus(), electronicChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateElectronicChannels(electronicChannelsResponse.getResponse());
    }
  }

  @Override
  public List<PhoneChannel> listPhoneChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.PhoneChannel>> phoneChannelsResponse = ptvApi.getPhoneChannelsApi().listServicePhoneChannels(ptvServiceId.getId(), null, null);
    if (!phoneChannelsResponse.isOk()) {
      logger.severe(String.format("Phone channels list of service %s reported [%d] %s", serviceId.toString(), phoneChannelsResponse.getStatus(), phoneChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translatePhoneChannels(phoneChannelsResponse.getResponse());
    }
  }

  @Override
  public List<PrintableFormChannel> listPrintableFormChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.PrintableFormChannel>> printableFormChannelsResponse = ptvApi.getPrintableFormChannelsApi().listServicePrintableFormChannels(ptvServiceId.getId(), null, null);
    if (!printableFormChannelsResponse.isOk()) {
      logger.severe(String.format("PrintableForm channels list of service %s reported [%d] %s", serviceId.toString(), printableFormChannelsResponse.getStatus(), printableFormChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translatePrintableFormChannels(printableFormChannelsResponse.getResponse());
    }
  }

  @Override
  public List<ServiceLocationChannel> listServiceLocationChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel>> serviceLocationChannelsResponse = ptvApi.getServiceLocationChannelsApi().listServiceServiceLocationChannels(ptvServiceId.getId(), null, null);
    if (!serviceLocationChannelsResponse.isOk()) {
      logger.severe(String.format("ServiceLocation channels list of service %s reported [%d] %s", serviceId.toString(), serviceLocationChannelsResponse.getStatus(), serviceLocationChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateServiceLocationChannels(serviceLocationChannelsResponse.getResponse());
    }
  }

  @Override
  public List<WebPageChannel> listWebPageChannelsChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.WebPageChannel>> webPageChannelsResponse = ptvApi.getWebPageChannelsApi().listServiceWebPageChannels(ptvServiceId.getId(), null, null);
    if (!webPageChannelsResponse.isOk()) {
      logger.severe(String.format("WebPage channels list of service %s reported [%d] %s", serviceId.toString(), webPageChannelsResponse.getStatus(), webPageChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateWebPageChannels(webPageChannelsResponse.getResponse());
    }
  }
  
}
