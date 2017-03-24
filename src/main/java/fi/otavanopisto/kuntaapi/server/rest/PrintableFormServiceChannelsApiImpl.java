package fi.otavanopisto.kuntaapi.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.kuntaapi.server.rest.PrintableFormServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

public class PrintableFormServiceChannelsApiImpl extends PrintableFormServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_PRINTABLE_FORM_CHANNEL_ID = "Invalid printable form service channel id %s";

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private HttpCacheController httpCacheController;
  
  @Override
  public Response findPrintableFormServiceChannel(String printableFormServiceChannelIdParam, @Context Request request) {
    PrintableFormServiceChannelId printableFormServiceChannelId = kuntaApiIdFactory.createPrintableFormServiceChannelId(printableFormServiceChannelIdParam);
    if (printableFormServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_PRINTABLE_FORM_CHANNEL_ID, printableFormServiceChannelIdParam));
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
  public Response listPrintableFormServiceChannels(Long firstResult, Long maxResults, @Context Request request) {
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
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

}
