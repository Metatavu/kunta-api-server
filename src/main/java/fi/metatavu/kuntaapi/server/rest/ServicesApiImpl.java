package fi.metatavu.kuntaapi.server.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.controllers.ClientContainer;
import fi.metatavu.kuntaapi.server.controllers.SecurityController;
import fi.metatavu.kuntaapi.server.controllers.ServiceController;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.server.rest.model.Service;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ({ "squid:S3306" })
public class ServicesApiImpl extends ServicesApi {

  private static final String INVALID_VALUE_FOR_SORT_DIR = "Invalid value for sortDir";
  private static final String INVALID_VALUE_FOR_SORT_BY = "Invalid value for sortBy";
  private static final String NOT_FOUND = "Not Found";
  private static final String NOT_IMPLEMENTED = "Not implemented";
  
  @Inject
  private ServiceController serviceController;

  @Inject
  private SecurityController securityController;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private RestValidator restValidator;

  @Inject
  private RestResponseBuilder restResponseBuilder;

  @Inject
  private ClientContainer clientContainer;
  
  @Override
  public Response createService(Service body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findService(String serviceIdParam, @Context Request request) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, serviceId);
    if (notModified != null) {
      return notModified;
    }

    Service service = serviceController.findService(serviceId);
    if (service != null) {
      return restResponseBuilder.sendModified(service, service.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listServices(String organizationIdParam, String search, String sortByParam, String sortDirParam, 
      Long firstResult, Long maxResults, String electronicServiceChannelIdParam, String phoneServiceChannelIdParam,
      String printableFormServiceChannelIdParam, String serviceLocationServiceChannelIdParam, String webPageServiceChannelIdParam,
      Request request) {
   
    ServiceSortBy sortBy = resolveServiceSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    ElectronicServiceChannelId electronicServiceChannelId = kuntaApiIdFactory.createElectronicServiceChannelId(electronicServiceChannelIdParam);
    PhoneServiceChannelId phoneServiceChannelId = kuntaApiIdFactory.createPhoneServiceChannelId(phoneServiceChannelIdParam);
    PrintableFormServiceChannelId printableFormServiceChannelId = kuntaApiIdFactory.createPrintableFormServiceChannelId(printableFormServiceChannelIdParam);
    ServiceLocationServiceChannelId serviceLocationServiceChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationServiceChannelIdParam);
    WebPageServiceChannelId webPageServiceChannelId = kuntaApiIdFactory.createWebPageServiceChannelId(webPageServiceChannelIdParam);
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    SearchResult<Service> result = serviceController.searchServices(organizationId, 
        electronicServiceChannelId,
        phoneServiceChannelId,
        printableFormServiceChannelId,
        serviceLocationServiceChannelId,
        webPageServiceChannelId,
        search, 
        sortBy, 
        sortDir, 
        firstResult, 
        maxResults);
    
    return restResponseBuilder.buildResponse(result, request);
  }
  
  @Override
  public Response updateService(String serviceIdParam, Service body, @Context Request request) {
    ServiceId serviceId = kuntaApiIdFactory.createServiceId(serviceIdParam);
    Service service = serviceController.findService(serviceId);
    
    if (service == null) {
      return createNotFound(NOT_FOUND);
    }
    
    OrganizationId mainResponsibleOrganizationId = serviceController.getServiceMainResponsibleOrganization(service);
    if (mainResponsibleOrganizationId == null) {
      return createInternalServerError("Failed to resolve main responsible organization id");
    }
    
    if (!securityController.hasOrganizationPermission(clientContainer.getClient(), mainResponsibleOrganizationId, ClientOrganizationPermission.UPDATE_SERVICES)) {
      return createForbidden("No permission to update service");
    }
    
    IntegrationResponse<Service> integrationResponse = serviceController.updateService(serviceId, body);
    if (integrationResponse.isOk()) {
      return restResponseBuilder.sendModified(integrationResponse.getEntity(), integrationResponse.getEntity().getId());
    } else {
      return restResponseBuilder.buildErrorResponse(integrationResponse);
    }
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

  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }

  private ServiceSortBy resolveServiceSortBy(String sortByParam) {
    ServiceSortBy sortBy = ServiceSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(ServiceSortBy.class, sortByParam);
    }
    return sortBy;
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

