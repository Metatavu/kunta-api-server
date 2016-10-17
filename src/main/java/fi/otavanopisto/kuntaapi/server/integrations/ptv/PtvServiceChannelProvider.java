package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
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
@Dependent
public class PtvServiceChannelProvider extends AbstractPtvProvider implements ServiceChannelProvider {

  private static final String SERVICE_TRANSLATE_FAILURE = "Failed to translate ptvServiceId %s into PTV service";

  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  private PtvServiceChannelProvider() {
  }

  @Override
  public List<ElectronicChannel> listElectronicChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.ElectronicChannel>> electronicChannelsResponse = ptvApi.getElectronicChannelsApi().listServiceElectronicChannels(serviceId.getId(), null, null);
    if (!electronicChannelsResponse.isOk()) {
      logger.severe(String.format("Electronic channels list of service %s reported [%d] %s", serviceId.toString(), electronicChannelsResponse.getStatus(), electronicChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateElectronicChannels(electronicChannelsResponse.getResponse());
    }
  }

  @Override
  public List<PhoneChannel> listPhoneChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.PhoneChannel>> phoneChannelsResponse = ptvApi.getPhoneChannelsApi().listServicePhoneChannels(serviceId.getId(), null, null);
    if (!phoneChannelsResponse.isOk()) {
      logger.severe(String.format("Phone channels list of service %s reported [%d] %s", serviceId.toString(), phoneChannelsResponse.getStatus(), phoneChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translatePhoneChannels(phoneChannelsResponse.getResponse());
    }
  }

  @Override
  public List<PrintableFormChannel> listPrintableFormChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.PrintableFormChannel>> printableFormChannelsResponse = ptvApi.getPrintableFormChannelsApi().listServicePrintableFormChannels(serviceId.getId(), null, null);
    if (!printableFormChannelsResponse.isOk()) {
      logger.severe(String.format("PrintableForm channels list of service %s reported [%d] %s", serviceId.toString(), printableFormChannelsResponse.getStatus(), printableFormChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translatePrintableFormChannels(printableFormChannelsResponse.getResponse());
    }
  }

  @Override
  public List<ServiceLocationChannel> listServiceLocationChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel>> serviceLocationChannelsResponse = ptvApi.getServiceLocationChannelsApi().listServiceServiceLocationChannels(serviceId.getId(), null, null);
    if (!serviceLocationChannelsResponse.isOk()) {
      logger.severe(String.format("ServiceLocation channels list of service %s reported [%d] %s", serviceId.toString(), serviceLocationChannelsResponse.getStatus(), serviceLocationChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateServiceLocationChannels(serviceLocationChannelsResponse.getResponse());
    }
  }

  @Override
  public List<WebPageChannel> listWebPageChannelsChannels(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format(SERVICE_TRANSLATE_FAILURE, serviceId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.WebPageChannel>> webPageChannelsResponse = ptvApi.getWebPageChannelsApi().listServiceWebPageChannels(serviceId.getId(), null, null);
    if (!webPageChannelsResponse.isOk()) {
      logger.severe(String.format("WebPage channels list of service %s reported [%d] %s", serviceId.toString(), webPageChannelsResponse.getStatus(), webPageChannelsResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateWebPageChannels(webPageChannelsResponse.getResponse());
    }
  }
  
}
