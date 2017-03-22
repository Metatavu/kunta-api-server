package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.WebPageServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

public class WebPageServiceChannelsApiImpl extends WebPageServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_WEBPAGE_CHANNEL_ID = "Invalid webpage service channel id %s";
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private HttpCacheController httpCacheController;

  @Override
  public Response findWebPageServiceChannel(String webPageServiceChannelIdParam, @Context Request request) {
    WebPageServiceChannelId webPageServiceChannelId = kuntaApiIdFactory.createWebPageServiceChannelId(webPageServiceChannelIdParam);
    if (webPageServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_WEBPAGE_CHANNEL_ID, webPageServiceChannelIdParam));
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
  public Response listWebPageServiceChannels(Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
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

}
