package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.cache.PtvOrganizationServiceCache;

/**
 * PTV Service provider
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvServiceProvider implements ServiceProvider {
  
  @Inject
  private PtvServiceCache ptvServiceCache;
  
  @Inject
  private PtvOrganizationServiceCache ptvOrganizationServiceCache;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
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
    List<OrganizationServiceId> organizationServiceIds = identifierRelationController.listOrganizationServiceIdsBySourceAndParentId(PtvConsts.IDENTIFIER_NAME, organizationId);
    
    List<ServiceId> result = new ArrayList<>(organizationServiceIds.size());
    
    for (OrganizationServiceId organizationServiceId : organizationServiceIds) {
      OrganizationService organizationService = ptvOrganizationServiceCache.get(organizationServiceId);
      if (organizationService != null) {
        result.add(new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, organizationService.getServiceId()));
      }
    }
    
    return result;
  }
  
}
