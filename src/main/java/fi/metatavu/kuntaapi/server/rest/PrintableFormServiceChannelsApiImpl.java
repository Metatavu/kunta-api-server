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
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.IntegrationResponse;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;

@RequestScoped
@Stateful
public class PrintableFormServiceChannelsApiImpl extends PrintableFormServiceChannelsApi {

  private static final String NOT_FOUND = "Not Found";
  private static final String INVALID_PRINTABLE_FORM_CHANNEL_ID = "Invalid printable form service channel id %s";
  private static final String INVALID_VALUE_FOR_SORT_DIR = "Invalid value for sortDir";
  private static final String INVALID_VALUE_FOR_SORT_BY = "Invalid value for sortBy";

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private ServiceController serviceController;

  @Inject
  private RestResponseBuilder restResponseBuilder;
  
  @Inject
  private ClientContainer clientContainer;

  @Inject
  private SecurityController securityController;
  
  @Override
  public Response findPrintableFormServiceChannel(String printableFormServiceChannelIdParam, @Context Request request) {
    PrintableFormServiceChannelId printableFormServiceChannelId = kuntaApiIdFactory.createPrintableFormServiceChannelId(printableFormServiceChannelIdParam);
    if (printableFormServiceChannelId == null) {
      return createBadRequest(String.format(INVALID_PRINTABLE_FORM_CHANNEL_ID, printableFormServiceChannelIdParam));
    }

    Response notModified = restResponseBuilder.getNotModified(request, printableFormServiceChannelId);
    if (notModified != null) {
      return notModified;
    }

    PrintableFormServiceChannel printableFormChannel = serviceController.findPrintableFormServiceChannel(printableFormServiceChannelId);
    if (printableFormChannel != null) {
      return restResponseBuilder.sendModified(printableFormChannel, printableFormChannel.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listPrintableFormServiceChannels(String organizationIdParam, String search, String sortByParam,
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
    
    return restResponseBuilder.buildResponse(serviceController.searchPrintableFormServiceChannels(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
  }

  @Override
  public Response updatePrintableFormServiceChannel(String printableFormServiceChannelIdParam, PrintableFormServiceChannel newPrintableFormChannel, Request request) {
    PrintableFormServiceChannelId printableFormServiceChannelId = kuntaApiIdFactory.createPrintableFormServiceChannelId(printableFormServiceChannelIdParam);
    PrintableFormServiceChannel printableFormServiceChannel = serviceController.findPrintableFormServiceChannel(printableFormServiceChannelId);
    
    if (printableFormServiceChannel == null) {
      return createNotFound(NOT_FOUND);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(printableFormServiceChannel.getOrganizationId());
    if (!securityController.hasOrganizationPermission(clientContainer.getClient(), organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS)) {
      return createForbidden("No permission to update service location service channel");
    }
    
    IntegrationResponse<PrintableFormServiceChannel> integrationResponse = serviceController.updatePrintableFormServiceChannel(printableFormServiceChannelId, newPrintableFormChannel);
    if (integrationResponse.isOk()) {
      return restResponseBuilder.sendModified(integrationResponse.getEntity(), integrationResponse.getEntity().getId());
    } else {
      return restResponseBuilder.buildErrorResponse(integrationResponse);
    }
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
