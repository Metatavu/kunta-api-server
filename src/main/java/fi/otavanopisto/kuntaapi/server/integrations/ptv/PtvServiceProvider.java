package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.restfulptv.client.ApiResponse;

/**
 * PTV Service provider
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvServiceProvider implements ServiceProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PtvServiceCache ptvServiceCache;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;

  @Override
  public Service findService(ServiceId serviceId) {
    return ptvServiceCache.get(serviceId);
  }

  @Override
  public List<Service> listServices(OrganizationId organizationId) {
    List<ServiceId> serviceIds;
    
    if (organizationId != null) {
      serviceIds = listOrganizationServiceIds(organizationId);
    } else {
      serviceIds = identifierController.listServiceIdsBySource(PtvConsts.IDENTIFIER_NAME);
    }
    
    List<Service> result = new ArrayList<>(serviceIds.size());
    for (ServiceId serviceId : serviceIds) {
      Service service = ptvServiceCache.get(serviceId);
      if (service != null) {
        result.add(service);
      }
    }
    
    return result;
  }
  
  private List<ServiceId> listOrganizationServiceIds(OrganizationId organizationId) {
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.severe(String.format("Failed to translate organizationId %s into PTV id", organizationId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.Service>> servicesResponse = ptvApi.getServicesApi()
        .listServices(ptvOrganizationId.getId(), null, null);
    
    if (!servicesResponse.isOk()) {
      logger.severe(String.format("Failed to list services [%d] %s", servicesResponse.getStatus(), servicesResponse.getMessage()));
      return Collections.emptyList();
    }
    
    List<fi.otavanopisto.restfulptv.client.model.Service> ptvServices = servicesResponse.getResponse();
    List<ServiceId> result = new ArrayList<>(ptvServices.size());
    for (fi.otavanopisto.restfulptv.client.model.Service ptvService : ptvServices) {
      result.add(new ServiceId(PtvConsts.IDENTIFIER_NAME, ptvService.getId()));
    }
    
    return result;
  }
  
}
