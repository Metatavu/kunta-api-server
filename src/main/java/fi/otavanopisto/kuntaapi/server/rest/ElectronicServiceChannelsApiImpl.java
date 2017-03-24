package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.ElectronicServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ElectronicServiceChannelsApiImpl extends ElectronicServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_ELECTRONIC_CHANNEL_ID = "Invalid electronic service channel id %s";
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private HttpCacheController httpCacheController;

  @Override
  public Response findElectronicServiceChannel(String electronicChannelIdParam, @Context Request request) {
    ElectronicServiceChannelId electronicChannelId = kuntaApiIdFactory.createElectronicServiceChannelId(electronicChannelIdParam);
    if (electronicChannelId == null) {
      return createBadRequest(String.format(INVALID_ELECTRONIC_CHANNEL_ID, electronicChannelIdParam));
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
  public Response listElectronicServiceChannels(Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
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

}
