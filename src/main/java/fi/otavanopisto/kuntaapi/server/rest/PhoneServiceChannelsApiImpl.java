package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.PhoneServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

public class PhoneServiceChannelsApiImpl extends PhoneServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_PHONE_CHANNEL_ID = "Invalid electronic phone service channel id %s";

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private HttpCacheController httpCacheController;

  @Override
  public Response findPhoneServiceChannel(String phoneChannelIdParam, @Context Request request) {
    PhoneServiceChannelId phoneServiceChannelId = kuntaApiIdFactory.createPhoneServiceChannelId(phoneChannelIdParam);
    if (phoneServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_PHONE_CHANNEL_ID, phoneChannelIdParam));
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
  public Response listPhoneServiceChannels(Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<PhoneServiceChannel> result = serviceController.listPhoneServiceChannels(firstResult, maxResults);
    
    return Response.ok(result).build();
  }

}
