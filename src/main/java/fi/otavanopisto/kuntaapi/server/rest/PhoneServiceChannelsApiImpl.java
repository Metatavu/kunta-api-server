package fi.otavanopisto.kuntaapi.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.kuntaapi.server.rest.PhoneServiceChannelsApi;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.ServiceController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

public class PhoneServiceChannelsApiImpl extends PhoneServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_PHONE_CHANNEL_ID = "Invalid electronic phone service channel id %s";
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
  public Response listPhoneServiceChannels(String organizationIdParam, String search, String sortByParam,
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
    
    return restResponseBuilder.buildResponse(serviceController.searchPhoneServiceChannels(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
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
