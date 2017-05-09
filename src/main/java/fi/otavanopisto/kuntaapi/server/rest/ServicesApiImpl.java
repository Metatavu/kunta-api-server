package fi.otavanopisto.kuntaapi.server.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.ServicesApi;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ServicesApiImpl extends ServicesApi {
  
  private static final String NOT_FOUND = "Not Found";
  private static final String NOT_IMPLEMENTED = "Not implemented";
  
  @Inject
  private ServiceController serviceController;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private RestResponseBuilder restResponseBuilder;
  
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
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (search == null) {
      return restResponseBuilder.buildResponse(serviceController.listServices(organizationId, firstResult, maxResults), null, request);
    } else {
      return restResponseBuilder.buildResponse(serviceController.searchServices(organizationId, search, firstResult, maxResults), request);
    }
  }
  
  @Override
  public Response updateService(String serviceId, Service body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findServiceElectronicChannel(String serviceIdParam, String electronicChannelIdParam, @Context Request request) {
    return redirect(String.format("/electronicServiceChannels/%s", electronicChannelIdParam));
  }

  @Override
  public Response findServicePhoneChannel(String serviceIdParam, String phoneChannelIdParam, @Context Request request) {
    return redirect(String.format("/phoneServiceChannels/%s", phoneChannelIdParam));
  }

  @Override
  public Response findServicePrintableFormChannel(String serviceIdParam, String printableFormChannelIdParam, @Context Request request) {
    return redirect(String.format("/printableFormServiceChannels/%s", printableFormChannelIdParam));
  }

  @Override
  public Response findServiceServiceLocationChannel(String serviceIdParam, String serviceLocationChannelIdParam, @Context Request request) {
    return redirect(String.format("/serviceLocationServiceChannels/%s", serviceLocationChannelIdParam));    
  }
  
  @Override
  public Response findServiceWebPageChannel(String serviceIdParam, String webPageChannelIdParam, @Context Request request) {
    return redirect(String.format("/webPageServiceChannels/%s", webPageChannelIdParam)); 
  }

  @Override
  public Response listServiceElectronicChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    return redirectServiceChannelList("/electronicServiceChannels", firstResult, maxResults);
  }

  @Override
  public Response listServicePhoneChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    return redirectServiceChannelList("/phoneServiceChannels", firstResult, maxResults);
  }
  
  @Override
  public Response listServicePrintableFormChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    return redirectServiceChannelList("/printableFormServiceChannels", firstResult, maxResults);
  }

  @Override
  public Response listServiceServiceLocationChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    return redirectServiceChannelList("/serviceLocationServiceChannels", firstResult, maxResults);
  }

  @Override
  public Response listServiceWebPageChannels(String serviceIdParam, Long firstResult, Long maxResults, @Context Request request) {
    return redirectServiceChannelList("/webPageServiceChannels", firstResult, maxResults);
  }
  
  private Response redirectServiceChannelList(String path, Long firstResult, Long maxResults) {
    StringBuilder pathBuilder = new StringBuilder(path);
    
    List<String> query = new ArrayList<>(2);
    
    if (firstResult != null) {
      query.add(String.format("firstResult=%d", firstResult));
    }
    
    if (maxResults != null) {
      query.add(String.format("maxResults=%d", maxResults));
    }
    
    if (!query.isEmpty()) {
      pathBuilder.append('?').append(StringUtils.join(query, '&'));
    }
    
    return redirect(pathBuilder.toString());
  }


  private Response redirect(String path) {
    return Response.temporaryRedirect(URI.create(path)).build();
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
  
}

