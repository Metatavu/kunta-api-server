package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.rest.ServicesApi;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;

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
  private HttpCacheController httpCacheController;
  
  @Override
  public Response createService(Service body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findService(String serviceIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, serviceId);
    if (notModified != null) {
      return notModified;
    }

    Service service = serviceController.findService(serviceId);
    if (service != null) {
      return httpCacheController.sendModified(service, service.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listServices(String organizationIdParam, String search, Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
    List<Service> services;
    if (search == null) {
      services = serviceController.listServices(organizationId, firstResult, maxResults);
    } else {
      services = serviceController.searchServices(organizationId, search, firstResult, maxResults);
    }
    
    List<String> ids = httpCacheController.getEntityIds(services);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(services, ids);
  }
  
  @Override
  public Response updateService(String serviceId, Service body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findServiceElectronicChannel(String serviceIdParam, String electronicChannelIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    ElectronicServiceChannelId electronicChannelId = toElectronicServiceChannelId(electronicChannelIdParam);
    if (electronicChannelId == null) {
      return createBadRequest(String.format(INVALID_ELECTRONIC_CHANNEL_ID, serviceIdParam));
    }
    
    Response notModified = httpCacheController.getNotModified(request, electronicChannelId);
    if (notModified != null) {
      return notModified;
    }

    ElectronicServiceChannel electronicChannel = serviceController.findElectronicServiceChannel(electronicChannelId);
    if (electronicChannel != null) {
      return httpCacheController.sendModified(electronicChannel, electronicChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findServicePhoneChannel(String serviceIdParam, String phoneChannelIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    PhoneServiceChannelId phoneServiceChannelId = toPhoneServiceChannelId(phoneChannelIdParam);
    if (phoneServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_PHONE_CHANNEL_ID, serviceIdParam));
    }
    
    Response notModified = httpCacheController.getNotModified(request, phoneServiceChannelId);
    if (notModified != null) {
      return notModified;
    }

    PhoneServiceChannel phoneChannel = serviceController.findPhoneServiceChannel(phoneServiceChannelId);
    if (phoneChannel != null) {
      return httpCacheController.sendModified(phoneChannel, phoneChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findServicePrintableFormChannel(String serviceIdParam, String printableFormChannelIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    PrintableFormServiceChannelId printableFormServiceChannelId = toPrintableFormServiceChannelId(printableFormChannelIdParam);
    if (printableFormServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_PRINTABLE_FORM_CHANNEL_ID, serviceIdParam));
    }

    Response notModified = httpCacheController.getNotModified(request, printableFormServiceChannelId);
    if (notModified != null) {
      return notModified;
    }

    PrintableFormServiceChannel printableFormChannel = serviceController.findPrintableFormServiceChannel(printableFormServiceChannelId);
    if (printableFormChannel != null) {
      return httpCacheController.sendModified(printableFormChannel, printableFormChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findServiceServiceLocationChannel(String serviceIdParam, String serviceLocationChannelIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    ServiceLocationServiceChannelId serviceLocationChannelId = toServiceLocationServiceChannelId(serviceLocationChannelIdParam);
    if (serviceLocationChannelId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_LOCATION_CHANNEL_ID, serviceIdParam));
    }

    Response notModified = httpCacheController.getNotModified(request, serviceLocationChannelId);
    if (notModified != null) {
      return notModified;
    }

    ServiceLocationServiceChannel serviceLocationChannel = serviceController.findServiceLocationServiceChannel(serviceLocationChannelId);
    if (serviceLocationChannel != null) {
      return httpCacheController.sendModified(serviceLocationChannel, serviceLocationChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response findServiceWebPageChannel(String serviceIdParam, String webPageChannelIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    WebPageServiceChannelId webPageServiceChannelId = toWebPageServiceChannelId(webPageChannelIdParam);
    if (webPageServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_WEBPAGE_CHANNEL_ID, serviceIdParam));
    }

    Response notModified = httpCacheController.getNotModified(request, webPageServiceChannelId);
    if (notModified != null) {
      return notModified;
    }

    WebPageServiceChannel webPageChannel = serviceController.findWebPageServiceChannel(webPageServiceChannelId);
    if (webPageChannel != null) {
      return httpCacheController.sendModified(webPageChannel, webPageChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listServiceElectronicChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<ElectronicServiceChannel> result = serviceController.listElectronicServiceChannels(firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listServicePhoneChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<PhoneServiceChannel> result = serviceController.listPhoneServiceChannels(firstResult, maxResults);
    return Response.ok(result).build();
  }
  
  @Override
  public Response listServicePrintableFormChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<PrintableFormServiceChannel> result = serviceController.listPrintableFormServiceChannels(firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listServiceServiceLocationChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<ServiceLocationServiceChannel> result = serviceController.listServiceLocationServiceChannels(firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listServiceWebPageChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<WebPageServiceChannel> result = serviceController.listWebPageServiceChannels(firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
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
  
  private OrganizationId toOrganizationId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private ServiceId toServiceId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private ElectronicServiceChannelId toElectronicServiceChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ElectronicServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private PhoneServiceChannelId toPhoneServiceChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PhoneServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private PrintableFormServiceChannelId toPrintableFormServiceChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PrintableFormServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private ServiceLocationServiceChannelId toServiceLocationServiceChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceLocationServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private WebPageServiceChannelId toWebPageServiceChannelId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new WebPageServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
}

