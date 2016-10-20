package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.restfulptv.client.ApiResponse;

/**
 * PTV Service provider
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvServiceProvider extends AbstractPtvProvider implements ServiceProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;

  @Override
  public Service findService(ServiceId serviceId) {
    ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvServiceId == null) {
      logger.severe(String.format("Failed to translate serviceId %s into PTV serviceId", serviceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.Service> serviceResponse = ptvApi.getServicesApi()
      .findService(ptvServiceId.getId());
    if (!serviceResponse.isOk()) {
      logger.severe(String.format("Failed to find service %s [%d] %s", ptvServiceId.toString(), serviceResponse.getStatus(), serviceResponse.getMessage()));
      return null;
    }

    return translateService(serviceResponse.getResponse());
  }

  @Override
  public List<Service> listServices(Long firstResult, Long maxResults) {
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.Service>> servicesResponse = ptvApi.getServicesApi()
      .listServices(firstResult, maxResults);
    if (!servicesResponse.isOk()) {
      logger.severe(String.format("Failed to list services [%d] %s", servicesResponse.getStatus(), servicesResponse.getMessage()));
      return Collections.emptyList();
    }

    return translateServices(servicesResponse.getResponse());
  }
  

}
