package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import javax.enterprise.context.ApplicationScoped;

/**
 * PTV Service provider
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvServiceProvider implements ServiceProvider {
  
  @Inject
  private PtvServiceResourceContainer ptvServiceResourceContainer;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public Service findService(ServiceId serviceId) {
    return ptvServiceResourceContainer.get(serviceId);
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
      Service service = ptvServiceResourceContainer.get(serviceId);
      if (service != null) {
        result.add(service);
      }
    }
    
    return result;
  }
  
  private List<ServiceId> listOrganizationServiceIds(OrganizationId organizationId) {
    return identifierRelationController.listServiceIdsBySourceAndParentId(PtvConsts.IDENTIFIER_NAME, organizationId);
  }
  
}
