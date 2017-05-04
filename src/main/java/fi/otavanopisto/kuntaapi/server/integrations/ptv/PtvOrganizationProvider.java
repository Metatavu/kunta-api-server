package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvOrganizationResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Organization provider for PTV
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvOrganizationProvider implements OrganizationProvider {
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private PtvOrganizationResourceContainer ptvOrganizationResourceContainer;
  
  @Override
  public Organization findOrganization(OrganizationId organizationId) {
    return ptvOrganizationResourceContainer.get(organizationId);
  }
  
  @Override
  @SuppressWarnings("squid:S135")
  public List<Organization> listOrganizations(String businessName, String businessCode) {
    List<OrganizationId> organizationIds = identifierController.listOrganizationIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<Organization> result = new ArrayList<>(organizationIds.size());
    
    for (OrganizationId organizationId : organizationIds) {
      Organization organization = ptvOrganizationResourceContainer.get(organizationId);
      if (organization != null && isAcceptable(organization, businessCode, businessName)) {
        result.add(organization);
      }
    }
    
    return result;
  }

  private boolean isAcceptable(Organization organization, String businessCode, String businessName) {
    if (StringUtils.isNotBlank(businessCode) && !StringUtils.equals(businessCode, organization.getBusinessCode())) {
      return false;
    }
    
    if (StringUtils.isNotBlank(businessName) && !StringUtils.equals(businessName, organization.getBusinessName())) {
      return false;
    }
    
    
    return true;
  }

}
