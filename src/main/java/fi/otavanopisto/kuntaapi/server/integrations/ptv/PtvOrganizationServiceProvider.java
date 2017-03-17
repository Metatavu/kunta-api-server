package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationServiceProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvOrganizationServiceResourceContainer;

/**
 * Organization service  provider for PTV
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvOrganizationServiceProvider implements OrganizationServiceProvider {
  
  @Inject
  private PtvOrganizationServiceResourceContainer ptvOrganizationServiceCache;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public OrganizationService findOrganizationService(OrganizationId organizationId, OrganizationServiceId organizationServiceId) {
    if (identifierRelationController.isChildOf(organizationId, organizationServiceId)) {
      return ptvOrganizationServiceCache.get(organizationServiceId);
    }
    
    return null;
  }

  @Override
  public List<OrganizationService> listOrganizationServices(OrganizationId organizationId) {
    List<OrganizationServiceId> organizationServiceIds = identifierRelationController.listOrganizationServiceIdsBySourceAndParentId(PtvConsts.IDENTIFIER_NAME, organizationId);
    List<OrganizationService> organizationServices = new ArrayList<>(organizationServiceIds.size());
    
    for (OrganizationServiceId organizationServiceId : organizationServiceIds) {
      OrganizationService organizationService = ptvOrganizationServiceCache.get(organizationServiceId);
      if (organizationService != null) {
        organizationServices.add(organizationService);
      }
    }
    
    return organizationServices;
  }

}
