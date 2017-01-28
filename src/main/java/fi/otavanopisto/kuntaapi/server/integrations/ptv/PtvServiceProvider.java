package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Service;
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
  private IdController idController;

  @Override
  public Service findService(ServiceId serviceId) {
    return ptvServiceCache.get(serviceId);
  }

  @Override
  public List<Service> listServices(OrganizationId organizationId) {
    OrganizationId ptvOrganizationId = null;
    if (organizationId != null) {
      ptvOrganizationId = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIFER_NAME);
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
      List<Service> result = new ArrayList<>(ptvServices.size());
      for (fi.otavanopisto.restfulptv.client.model.Service ptvService : ptvServices) {
        ServiceId ptvServiceId = new ServiceId(PtvConsts.IDENTIFIFER_NAME, ptvService.getId());
        result.add(ptvServiceCache.get(ptvServiceId));
      }
      
      return result;
    } else {
      return new ArrayList<>(ptvServiceCache.getEntities());
    }
  }
  

}
