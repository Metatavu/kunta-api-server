package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.ServiceLocationServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

public class ServiceLocationServiceChannelsApiImpl extends ServiceLocationServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_SERVICE_LOCATION_CHANNEL_ID = "Invalid service location service channel id %s";

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private HttpCacheController httpCacheController;
  

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
  public Response listServiceLocationServiceChannels(String search, Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<ServiceLocationServiceChannel> result = serviceController.listServiceLocationServiceChannels(search, firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

}
