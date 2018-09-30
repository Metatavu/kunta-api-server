package fi.metatavu.kuntaapi.server.rest;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.kuntaapi.server.controllers.ClientContainer;
import fi.metatavu.kuntaapi.server.controllers.SecurityController;
import fi.metatavu.kuntaapi.server.controllers.ServiceController;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;

@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ElectronicServiceChannelsApiImpl extends ElectronicServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_ELECTRONIC_CHANNEL_ID = "Invalid electronic service channel id %s";
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
  private RestResponseBuilder restResponseBuilder;
  
  @Inject
  private ClientContainer clientContainer;

  @Override
  public Response findElectronicServiceChannel(String electronicChannelIdParam, @Context Request request) {
    ElectronicServiceChannelId electronicChannelId = kuntaApiIdFactory.createElectronicServiceChannelId(electronicChannelIdParam);
    if (electronicChannelId == null) {
      return createBadRequest(String.format(INVALID_ELECTRONIC_CHANNEL_ID, electronicChannelIdParam));
    }
    
    Response notModified = restResponseBuilder.getNotModified(request, electronicChannelId);
    if (notModified != null) {
      return notModified;
    }

    ElectronicServiceChannel electronicChannel = serviceController.findElectronicServiceChannel(electronicChannelId);
    if (electronicChannel != null) {
      return restResponseBuilder.sendModified(electronicChannel, electronicChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listElectronicServiceChannels(String organizationIdParam, String search, String sortByParam,
      String sortDirParam, Long firstResult, Long maxResults, Request request) {
    
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    ServiceChannelSortBy sortBy = resolveElectronicServiceChannelSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    return restResponseBuilder.buildResponse(serviceController.searchElectronicServiceChannels(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
  }

  @Override
  public Response updateElectronicServiceChannel(String electronicServiceChannelIdParam, ElectronicServiceChannel newElectronicChannel, Request request) {
    ElectronicServiceChannelId electronicServiceChannelId = kuntaApiIdFactory.createElectronicServiceChannelId(electronicServiceChannelIdParam);
    ElectronicServiceChannel electronicServiceChannel = serviceController.findElectronicServiceChannel(electronicServiceChannelId);
    
    if (electronicServiceChannel == null) {
      return createNotFound(NOT_FOUND);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(electronicServiceChannel.getOrganizationId());
    if (!securityController.hasOrganizationPermission(clientContainer.getClient(), organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS)) {
      return createForbidden("No permission to update service location service channel");
    }
    
    IntegrationResponse<ElectronicServiceChannel> integrationResponse = serviceController.updateElectronicServiceChannel(electronicServiceChannelId, newElectronicChannel);
    if (integrationResponse.isOk()) {
      return restResponseBuilder.sendModified(integrationResponse.getEntity(), integrationResponse.getEntity().getId());
    } else {
      return restResponseBuilder.buildErrorResponse(integrationResponse);
    }
  }
  
  private ServiceChannelSortBy resolveElectronicServiceChannelSortBy(String sortByParam) {
    ServiceChannelSortBy sortBy = ServiceChannelSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(ServiceChannelSortBy.class, sortByParam);
    }
    
    return sortBy;
  }

  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }


}
