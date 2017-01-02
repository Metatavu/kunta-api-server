package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;

public interface OrganizationServiceProvider {
  
  public OrganizationService findOrganizationService(OrganizationId organizationId, OrganizationServiceId organizationServiceId);
  
  public List<OrganizationService> listOrganizationServices(OrganizationId organizationId);
  
}
