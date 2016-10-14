package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.integrations.IdController;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.restfulptv.client.ApiResponse;

/**
 * Organization provider for PTV
 * 
 * @author Antti Leppä
 */
@Dependent
public class PtvOrganizationProvider extends AbstractPtvProvider implements OrganizationProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;

  private PtvOrganizationProvider() {
  }

  @Override
  public Organization findOrganization(OrganizationId organizationId) {
    OrganizationId ptvOrganization = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (ptvOrganization == null) {
      logger.severe(String.format("Failed to translate organizationId %s into PTV organizationId", organizationId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.restfulptv.client.model.Organization> ptvOrganizationResponse = ptvApi.getOrganizationApi()
        .findOrganization(ptvOrganization.getId());
    if (!ptvOrganizationResponse.isOk()) {
      logger.severe(String.format("Organization %s reported [%d] %s", ptvOrganization.getId(), ptvOrganizationResponse.getStatus(), ptvOrganizationResponse.getMessage()));
    } else {
      return transformOrganization(ptvOrganizationResponse.getResponse());
    }
    
    return null;
  }
  
  @Override
  @SuppressWarnings("squid:S135")
  public List<Organization> listOrganizations(String businessName, String businessCode) {
    List<Organization> result = new ArrayList<>();
    
    ApiResponse<List<fi.otavanopisto.restfulptv.client.model.Organization>> listResponse = ptvApi.getOrganizationApi().listOrganizations(null, null);
    if (!listResponse.isOk()) {
      logger.severe(String.format("Organizations listing reported [%d] %s", listResponse.getStatus(), listResponse.getMessage()));
      return Collections.emptyList();
    }
    
    for (fi.otavanopisto.restfulptv.client.model.Organization ptvOrganization : listResponse.getResponse()) {
      if (StringUtils.isNotBlank(businessCode) && !StringUtils.equals(businessCode, ptvOrganization.getBusinessCode())) {
        continue;
      }
    
      if (StringUtils.isNotBlank(businessName) && !StringUtils.equals(businessName, ptvOrganization.getBusinessName())) {
        continue;
      } 
      
      Organization organization = transformOrganization(ptvOrganization);
      if (organization != null) {
        result.add(organization);
      }
    }
    
    return result;
  }

}
