package fi.metatavu.kuntaapi.server.rest;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.kuntaapi.server.rest.ServiceLocationServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.controllers.ClientContainer;
import fi.metatavu.kuntaapi.server.controllers.HttpCacheController;
import fi.metatavu.kuntaapi.server.controllers.SecurityController;
import fi.metatavu.kuntaapi.server.controllers.ServiceController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;

@RequestScoped
@Stateful
public class ServiceLocationServiceChannelsApiImpl extends ServiceLocationServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_SERVICE_LOCATION_CHANNEL_ID = "Invalid service location service channel id %s";
  private static final String INVALID_VALUE_FOR_SORT_DIR = "Invalid value for sortDir";
  private static final String INVALID_VALUE_FOR_SORT_BY = "Invalid value for sortBy";
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private SecurityController securityController;

  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private ClientContainer clientContainer;
  
  @Inject
  private RestResponseBuilder restResponseBuilder;
  
  @Override
  public Response findServiceLocationServiceChannel(String serviceLocationServiceChannelId, @Context Request request) {
    ServiceLocationServiceChannelId serviceLocationChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationServiceChannelId);
    if (serviceLocationChannelId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_LOCATION_CHANNEL_ID, serviceLocationServiceChannelId));
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
  public Response updateServiceLocationServiceChannel(String serviceLocationServiceChannelIdParam, ServiceLocationServiceChannel newServiceLocationChannel, Request request) {
    ServiceLocationServiceChannelId serviceLocationServiceChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationServiceChannelIdParam);
    ServiceLocationServiceChannel serviceLocationServiceChannel = serviceController.findServiceLocationServiceChannel(serviceLocationServiceChannelId);
    
    if (serviceLocationServiceChannel == null) {
      return createNotFound(NOT_FOUND);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(serviceLocationServiceChannel.getOrganizationId());
    if (!securityController.hasOrganizationPermission(clientContainer.getClient(), organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS)) {
      return createForbidden("No permission to update service location service channel");
    }
    
    IntegrationResponse<ServiceLocationServiceChannel> integrationResponse = serviceController.updateServiceLocationServiceChannel(serviceLocationServiceChannelId, newServiceLocationChannel);
    if (integrationResponse.isOk()) {
      return httpCacheController.sendModified(integrationResponse.getEntity(), integrationResponse.getEntity().getId());
    } else {
      return restResponseBuilder.buildErrorResponse(integrationResponse);
    }
  }

  @Override
  public Response listServiceLocationServiceChannels(String organizationIdParam, String search, String sortByParam,
      String sortDirParam, Long firstResult, Long maxResults, Request request) {

    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    ServiceChannelSortBy sortBy = resolveServiceChannelSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    return restResponseBuilder.buildResponse(serviceController.searchServiceLocationServiceChannels(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
  }

  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }

  private ServiceChannelSortBy resolveServiceChannelSortBy(String sortByParam) {
    ServiceChannelSortBy sortBy = ServiceChannelSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(ServiceChannelSortBy.class, sortByParam);
    }
    
    return sortBy;
  }
  
}
