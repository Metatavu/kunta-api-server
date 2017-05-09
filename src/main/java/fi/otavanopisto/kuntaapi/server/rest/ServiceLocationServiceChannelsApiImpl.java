package fi.otavanopisto.kuntaapi.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.kuntaapi.server.rest.ServiceLocationServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceLocationServiceChannelSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

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
  private HttpCacheController httpCacheController;
  
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
  public Response listServiceLocationServiceChannels(String organizationIdParam, String search, String sortByParam,
      String sortDirParam, Long firstResult, Long maxResults, Request request) {

    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    ServiceLocationServiceChannelSortBy sortBy = resolveServiceLocationServiceChannelSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (search != null || organizationId != null) {
      return restResponseBuilder.buildResponse(serviceController.searchServiceLocationServiceChannels(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
    }
    
    return restResponseBuilder.buildResponse(serviceController.listServiceLocationServiceChannels(firstResult, maxResults), null, request);
  }

  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }

  private ServiceLocationServiceChannelSortBy resolveServiceLocationServiceChannelSortBy(String sortByParam) {
    ServiceLocationServiceChannelSortBy sortBy = ServiceLocationServiceChannelSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(ServiceLocationServiceChannelSortBy.class, sortByParam);
    }
    
    return sortBy;
  }
  
}
