package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.OrganizationService;
import fi.otavanopisto.restfulptv.client.ApiResponse;

/**
 * Organization provider for PTV
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvOrganizationServiceProvider extends AbstractPtvProvider implements OrganizationServiceProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  @Override
  public OrganizationService findOrganizationService(OrganizationId organizationId, OrganizationServiceId organizationServiceId) {
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvOrganizationId == null) {
      logger.severe(String.format("Failed to translate organizationId %s into PTV organizationId", organizationId.toString()));
      return null;
    }
    
    OrganizationServiceId ptvOrganizationServiceId = idController.translateOrganizationServiceId(organizationServiceId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvOrganizationServiceId == null) {
      logger.severe(String.format("Failed to translate organizationServiceId %s into PTV organizationServiceId", organizationServiceId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.OrganizationService> ptvResponse = ptvApi.getOrganizationServicesApi()
        .findOrganizationService(ptvOrganizationId.getId(), ptvOrganizationServiceId.getId());
    
    if (!ptvResponse.isOk()) {
      logger.severe(String.format("Organization service %s reported [%d] %s", ptvOrganizationServiceId.getId(), ptvResponse.getStatus(), ptvResponse.getMessage()));
      return null;
    } else {
      return translateOrganizationService(organizationId, ptvResponse.getResponse());
    }
  }

  @Override
  public List<OrganizationService> listOrganizationServices(OrganizationId organizationId) {
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIFER_NAME);
    if (ptvOrganizationId == null) {
      logger.severe(String.format("Failed to translate organizationId %s into PTV organizationId", organizationId.toString()));
      return Collections.emptyList();
    }
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.OrganizationService>> ptvResponse = ptvApi.getOrganizationServicesApi()
        .listOrganizationOrganizationServices(ptvOrganizationId.getId(), null, null);
    
    if (!ptvResponse.isOk()) {
      logger.severe(String.format("Organization service list %s reported [%d] %s", ptvOrganizationId.getId(), ptvResponse.getStatus(), ptvResponse.getMessage()));
      return Collections.emptyList();
    } else {
      return translateOrganizationServices(organizationId, ptvResponse.getResponse());
    }
  }

}
