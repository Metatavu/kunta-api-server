package fi.otavanopisto.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ServicesApiImpl extends ServicesApi {
  
  private static final String MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER = "maxResults must by a positive integer";
  private static final String FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER = "firstResult must by a positive integer";
  private static final String INVALID_SERVICE_ID = "Invalid service id %s";
  private static final String INVALID_ELECTRONIC_CHANNEL_ID = "Invalid electronic service channel id %s";
  private static final String INVALID_PHONE_CHANNEL_ID = "Invalid electronic phone service channel id %s";
  private static final String INVALID_SERVICE_LOCATION_CHANNEL_ID = "Invalid service location service channel id %s";
  private static final String INVALID_PRINTABLE_FORM_CHANNEL_ID = "Invalid printable form service channel id %s";
  private static final String INVALID_WEBPAGE_CHANNEL_ID = "Invalid webpage service channel id %s";
  private static final String NOT_FOUND = "Not Found";
  private static final String NOT_IMPLEMENTED = "Not implemented";
  
  @Inject
  private ServiceController serviceController;
  
  @Inject
  private Instance<ServiceChannelProvider> serviceChannelProviders;
  
  @Override
  public Response createService(Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findService(String serviceIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    
    Service service = serviceController.findService(serviceId);
    if (service != null) {
      return Response.ok(service)
        .build();
    }
    
    return Response
        .status(Status.NOT_FOUND)
        .entity(NOT_FOUND)
        .build();
  }
  
  @Override
  public Response listServices(String search, Long firstResult, Long maxResults) {
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    if (search == null) {
      return Response.ok(serviceController.listServices(firstResult, maxResults)).build();
    } else {
      return Response.ok(serviceController.searchServices(search, firstResult, maxResults)).build();
    }
  }
  
  @Override
  public Response updateService(String serviceId, Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response createServiceElectronicChannel(String serviceId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePhoneChannel(String serviceId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePrintableFormChannel(String serviceId, PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceServiceLocationChannel(String serviceId, ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceWebPageChannel(String serviceId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceElectronicChannel(String serviceIdParam, String electronicChannelIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    ElectronicServiceChannelId electronicChannelId = toElectronicChannelId(electronicChannelIdParam);
    if (electronicChannelId == null) {
      return createBadRequest(String.format(INVALID_ELECTRONIC_CHANNEL_ID, serviceIdParam));
    }
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      ElectronicChannel electronicChannel = serviceChannelProvider.findElectronicChannel(serviceId, electronicChannelId);
      if (electronicChannel != null) {
        return Response.ok(electronicChannel)
            .build();
      }
    }
    
    return Response
      .status(Status.NOT_FOUND)
      .entity(NOT_FOUND)
      .build();
  }

  @Override
  public Response findServicePhoneChannel(String serviceIdParam, String phoneChannelIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    PhoneChannelId phoneChannelId = toPhoneChannelId(phoneChannelIdParam);
    if (phoneChannelId == null) {
      return createBadRequest(String.format(INVALID_PHONE_CHANNEL_ID, serviceIdParam));
    }
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      PhoneChannel phoneChannel = serviceChannelProvider.findPhoneChannel(serviceId, phoneChannelId);
      if (phoneChannel != null) {
        return Response.ok(phoneChannel)
            .build();
      }
    }
    
    return Response
      .status(Status.NOT_FOUND)
      .entity(NOT_FOUND)
      .build();
  }

  @Override
  public Response findServicePrintableFormChannel(String serviceIdParam, String printableFormChannelIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    PrintableFormChannelId printableFormChannelId = toPrintableFormChannelId(printableFormChannelIdParam);
    if (printableFormChannelId == null) {
      return createBadRequest(String.format(INVALID_PRINTABLE_FORM_CHANNEL_ID, serviceIdParam));
    }
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      PrintableFormChannel printableFormChannel = serviceChannelProvider.findPrintableFormChannel(serviceId, printableFormChannelId);
      if (printableFormChannel != null) {
        return Response.ok(printableFormChannel)
            .build();
      }
    }

    return Response
      .status(Status.NOT_FOUND)
      .entity(NOT_FOUND)
      .build();
  }

  @Override
  public Response findServiceServiceLocationChannel(String serviceIdParam, String serviceLocationChannelIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    ServiceLocationChannelId serviceLocationChannelId = toServiceLocationChannelId(serviceLocationChannelIdParam);
    if (serviceLocationChannelId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_LOCATION_CHANNEL_ID, serviceIdParam));
    }
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      ServiceLocationChannel serviceLocationChannel = serviceChannelProvider.findServiceLocationChannel(serviceId, serviceLocationChannelId);
      if (serviceLocationChannel != null) {
        return Response.ok(serviceLocationChannel)
            .build();
      }
    }
    
    return Response
      .status(Status.NOT_FOUND)
      .entity(NOT_FOUND)
      .build();
  }

  @Override
  public Response findServiceWebPageChannel(String serviceIdParam, String webPageChannelIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    WebPageChannelId webPageChannelId = toWebPageChannelId(webPageChannelIdParam);
    if (webPageChannelId == null) {
      return createBadRequest(String.format(INVALID_WEBPAGE_CHANNEL_ID, serviceIdParam));
    }
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      WebPageChannel webPageChannel = serviceChannelProvider.findWebPageChannelChannel(serviceId, webPageChannelId);
      if (webPageChannel != null) {
        return Response.ok(webPageChannel)
            .build();
      }
    }
    
    return Response
      .status(Status.NOT_FOUND)
      .entity(NOT_FOUND)
      .build();
  }

  @Override
  public Response listServiceElectronicChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<ElectronicChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listElectronicChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServicePhoneChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<PhoneChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPhoneChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServicePrintableFormChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<PrintableFormChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPrintableFormChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServiceServiceLocationChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<ServiceLocationChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listServiceLocationChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServiceWebPageChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<WebPageChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listWebPageChannelsChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response updatePhoneChannel(String serviceId, String phoneChannelId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updatePrintableFormChannel(String serviceId, String printableFormChannelId,
      PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceElectronicChannel(String serviceId, String electronicChannelId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceLocationChannel(String serviceId, String serviceLocationChannelId,
      ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateWebPageChannel(String serviceId, String webPageChannelId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  private Response validateListLimitParams(Long firstResult, Long maxResults) {
    if (firstResult != null && firstResult < 0) {
      return createBadRequest(FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    if (maxResults != null && maxResults < 0) {
      return createBadRequest(MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    return null;
  }
  
  private ServiceId toServiceId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private ElectronicServiceChannelId toElectronicChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ElectronicServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private PhoneChannelId toPhoneChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PhoneChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private PrintableFormChannelId toPrintableFormChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PrintableFormChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private ServiceLocationChannelId toServiceLocationChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceLocationChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private WebPageChannelId toWebPageChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new WebPageChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private List<ServiceChannelProvider> getServiceChannelProviders() {
    List<ServiceChannelProvider> result = new ArrayList<>();
    
    Iterator<ServiceChannelProvider> iterator = serviceChannelProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}

