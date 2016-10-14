package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.rest.model.OrganizationService;

public interface OrganizationServiceProvider {
  
  public OrganizationService findOrganizationService(OrganizationId organizationId, OrganizationServiceId organizationServiceId);
  
  public List<OrganizationService> listOrganizationServices(OrganizationId organizationId);
  
}
