package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.restfulptv.client.ApiResponse;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@RequestScoped
public class PtvServiceChannelProvider implements ServiceChannelProvider {

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
  private PtvTranslator ptvTranslator;
  
  @Inject
  private IdController idController;
  
  @Override
  public ElectronicServiceChannel findElectronicServiceChannel(ElectronicServiceChannelId electronicServiceChannelId) {
    // TODO: FIXME!
    return null;
//    ElectronicServiceChannelId ptvElectronicServiceChannelId = idController.translateElectronicServiceChannelId(electronicServiceChannelId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvElectronicServiceChannelId == null) {
//      logger.severe(String.format(ELECTRONIC_SERVICE_CHANNEL_TRANSLATE_FAILURE, electronicServiceChannelId.toString()));
//      return null;
//    }
//    
//    ApiResponse<fi.metatavu.restfulptv.client.model.ElectronicServiceChannel> electronicChannelResponse = ptvApi.getElectronicServiceChannelsApi().findServiceElectronicServiceChannel(ptvElectronicServiceChannelId.getId());
//    if (!electronicChannelResponse.isOk()) {
//      logger.severe(String.format("Electronic channels list reported [%d] %s", electronicChannelResponse.getStatus(), electronicChannelResponse.getMessage()));
//      return null;
//    } else {
//      return ptvTranslator.translateElectronicServiceChannel(electronicChannelResponse.getResponse());
//    }
  }
  
  @Override
  public PhoneServiceChannel findPhoneServiceChannel(PhoneServiceChannelId phoneServiceChannelId) {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    PhoneServiceChannelId ptvPhoneServiceChannelId = idController.translatePhoneServiceChannelId(phoneServiceChannelId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvPhoneServiceChannelId == null) {
//      logger.severe(String.format(PHONE_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    ApiResponse<fi.metatavu.restfulptv.client.model.PhoneServiceChannel> phoneChannelResponse = ptvApi.getPhoneServiceChannelsApi().findServicePhoneServiceChannel(ptvServiceId.getId(), ptvPhoneServiceChannelId.getId());
//    if (!phoneChannelResponse.isOk()) {
//      logger.severe(String.format("Phone channels list of service %s reported [%d] %s", serviceId.toString(), phoneChannelResponse.getStatus(), phoneChannelResponse.getMessage()));
//      return null;
//    } else {
//      return ptvTranslator.translatePhoneServiceChannel(phoneChannelResponse.getResponse());
//    }
  }
  
  @Override
  public PrintableFormServiceChannel findPrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId) {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    PrintableFormServiceChannelId ptvPrintableFormServiceChannelId = idController.translatePrintableFormServiceChannelId(printableFormServiceChannelId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvPrintableFormServiceChannelId == null) {
//      logger.severe(String.format(PRINTABLE_FORM_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    ApiResponse<fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel> printableFormChannelResponse = ptvApi.getPrintableFormServiceChannelsApi().findServicePrintableFormServiceChannel(ptvServiceId.getId(), ptvPrintableFormServiceChannelId.getId());
//    if (!printableFormChannelResponse.isOk()) {
//      logger.severe(String.format("Printable form channels list of service %s reported [%d] %s", serviceId.toString(), printableFormChannelResponse.getStatus(), printableFormChannelResponse.getMessage()));
//      return null;
//    } else {
//      return ptvTranslator.translatePrintableFormServiceChannel(printableFormChannelResponse.getResponse());
//    }
  }
  
  @Override
  public ServiceLocationServiceChannel findServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId) {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(serviceLocationChannelId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceLocationServiceChannelId == null) {
//      logger.severe(String.format(SERVICE_LOCATION_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    ApiResponse<fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel> serviceLocationChannelResponse = ptvApi.getServiceLocationServiceChannelsApi().findServiceServiceLocationServiceChannel(ptvServiceId.getId(), ptvServiceLocationServiceChannelId.getId());
//    if (!serviceLocationChannelResponse.isOk()) {
//      logger.severe(String.format("Service location channels list of service %s reported [%d] %s", serviceId.toString(), serviceLocationChannelResponse.getStatus(), serviceLocationChannelResponse.getMessage()));
//      return null;
//    } else {
//      return ptvTranslator.translateServiceLocationServiceChannel(serviceLocationChannelResponse.getResponse());
//    }
  }
  
  @Override
  public WebPageServiceChannel findWebPageServiceChannelChannel(WebPageServiceChannelId webPageServiceChannelId) {
    // TODO: FIXME!
    return null;
//    
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    WebPageServiceChannelId ptvWebPageServiceChannelId = idController.translateWebPageServiceChannelId(webPageServiceChannelId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvWebPageServiceChannelId == null) {
//      logger.severe(String.format(WEBPAGE_SERVICE_CHANNEL_TRANSLATE_FAILURE, serviceId.toString()));
//      return null;
//    }
//    
//    ApiResponse<fi.metatavu.restfulptv.client.model.WebPageServiceChannel> webPageChannelResponse = ptvApi.getWebPageServiceChannelsApi().findServiceWebPageServiceChannel(ptvServiceId.getId(), ptvWebPageServiceChannelId.getId());
//    if (!webPageChannelResponse.isOk()) {
//      logger.severe(String.format("Web page channels list of service %s reported [%d] %s", serviceId.toString(), webPageChannelResponse.getStatus(), webPageChannelResponse.getMessage()));
//      return null;
//    } else {
//      return ptvTranslator.translateWebPageServiceChannel(webPageChannelResponse.getResponse());
//    }
  }
  
  @Override
  public List<ElectronicServiceChannel> listElectronicServiceChannels() {
    // TODO: FIXME!
    return null;
//    
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return Collections.emptyList();
//    }
//    
//    ApiResponse<List<fi.metatavu.restfulptv.client.model.ElectronicServiceChannel>> electronicChannelsResponse = ptvApi.getElectronicServiceChannelsApi().listServiceElectronicServiceChannels(ptvServiceId.getId(), null, null);
//    if (!electronicChannelsResponse.isOk()) {
//      logger.severe(String.format("Electronic channels list of service %s reported [%d] %s", serviceId.toString(), electronicChannelsResponse.getStatus(), electronicChannelsResponse.getMessage()));
//      return Collections.emptyList();
//    } else {
//      return ptvTranslator.translateElectronicServiceChannels(electronicChannelsResponse.getResponse());
//    }
  }

  @Override
  public List<PhoneServiceChannel> listPhoneServiceChannels() {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return Collections.emptyList();
//    }
//    
//    ApiResponse<List<fi.metatavu.restfulptv.client.model.PhoneServiceChannel>> phoneChannelsResponse = ptvApi.getPhoneServiceChannelsApi().listServicePhoneServiceChannels(ptvServiceId.getId(), null, null);
//    if (!phoneChannelsResponse.isOk()) {
//      logger.severe(String.format("Phone channels list of service %s reported [%d] %s", serviceId.toString(), phoneChannelsResponse.getStatus(), phoneChannelsResponse.getMessage()));
//      return Collections.emptyList();
//    } else {
//      return ptvTranslator.translatePhoneServiceChannels(phoneChannelsResponse.getResponse());
//    }
  }

  @Override
  public List<PrintableFormServiceChannel> listPrintableFormServiceChannels() {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return Collections.emptyList();
//    }
//    
//    ApiResponse<List<fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel>> printableFormChannelsResponse = ptvApi.getPrintableFormServiceChannelsApi().listServicePrintableFormServiceChannels(ptvServiceId.getId(), null, null);
//    if (!printableFormChannelsResponse.isOk()) {
//      logger.severe(String.format("PrintableForm channels list of service %s reported [%d] %s", serviceId.toString(), printableFormChannelsResponse.getStatus(), printableFormChannelsResponse.getMessage()));
//      return Collections.emptyList();
//    } else {
//      return ptvTranslator.translatePrintableFormServiceChannels(printableFormChannelsResponse.getResponse());
//    }
  }

  @Override
  public List<ServiceLocationServiceChannel> listServiceLocationServiceChannels() {
    // TODO: FIXME!
    return null;
//    
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return Collections.emptyList();
//    }
//    
//    ApiResponse<List<fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel>> serviceLocationChannelsResponse = ptvApi.getServiceLocationServiceChannelsApi().listServiceServiceLocationServiceChannels(ptvServiceId.getId(), null, null);
//    if (!serviceLocationChannelsResponse.isOk()) {
//      logger.severe(String.format("ServiceLocation channels list of service %s reported [%d] %s", serviceId.toString(), serviceLocationChannelsResponse.getStatus(), serviceLocationChannelsResponse.getMessage()));
//      return Collections.emptyList();
//    } else {
//      return ptvTranslator.translateServiceLocationServiceChannels(serviceLocationChannelsResponse.getResponse());
//    }
  }

  @Override
  public List<WebPageServiceChannel> listWebPageServiceChannelsChannels() {
    // TODO: FIXME!
    return null;
//    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
//    if (ptvServiceId == null) {
//      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
//      return Collections.emptyList();
//    }
//    
//    ApiResponse<List<fi.metatavu.restfulptv.client.model.WebPageServiceChannel>> webPageChannelsResponse = ptvApi.getWebPageServiceChannelsApi().listServiceWebPageServiceChannels(ptvServiceId.getId(), null, null);
//    if (!webPageChannelsResponse.isOk()) {
//      logger.severe(String.format("WebPage channels list of service %s reported [%d] %s", serviceId.toString(), webPageChannelsResponse.getStatus(), webPageChannelsResponse.getMessage()));
//      return Collections.emptyList();
//    } else {
//      return ptvTranslator.translateWebPageServiceChannels(webPageChannelsResponse.getResponse());
//    }
  }
  
}
