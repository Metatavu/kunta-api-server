package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationProvider;
import fi.metatavu.restfulptv.client.ApiResponse;

/**
 * Organization provider for PTV
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class PtvOrganizationProvider implements OrganizationProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private PtvTranslator ptvTranslator;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PtvIdFactory ptvIdFactory;
  
  @Inject
  private IdController idController;

  @Override
  public Organization findOrganization(OrganizationId organizationId) {
    OrganizationId ptvOrganization = idController.translateOrganizationId(organizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganization == null) {
      logger.severe(String.format("Failed to translate organizationId %s into PTV organizationId", organizationId.toString()));
      return null;
    }
    
    ApiResponse<fi.metatavu.restfulptv.client.model.Organization> ptvOrganizationResponse = ptvApi.getOrganizationApi()
        .findOrganization(ptvOrganization.getId());
    if (!ptvOrganizationResponse.isOk()) {
      logger.severe(String.format("Organization %s reported [%d] %s", ptvOrganization.getId(), ptvOrganizationResponse.getStatus(), ptvOrganizationResponse.getMessage()));
    } else {
      return ptvTranslator.translateOrganization(organizationId, ptvOrganizationResponse.getResponse());
    }
    
    return null;
  }
  
  @Override
  @SuppressWarnings("squid:S135")
  public List<Organization> listOrganizations(String businessName, String businessCode) {
    List<Organization> result = new ArrayList<>();
    
    ApiResponse<List<fi.metatavu.restfulptv.client.model.Organization>> listResponse = ptvApi.getOrganizationApi().listOrganizations(null, null);
    if (!listResponse.isOk()) {
      logger.severe(String.format("Organizations listing reported [%d] %s", listResponse.getStatus(), listResponse.getMessage()));
      return Collections.emptyList();
    }
    
    for (fi.metatavu.restfulptv.client.model.Organization ptvOrganization : listResponse.getResponse()) {
      if (StringUtils.isNotBlank(businessCode) && !StringUtils.equals(businessCode, ptvOrganization.getBusinessCode())) {
        continue;
      }
    
      if (StringUtils.isNotBlank(businessName) && !StringUtils.equals(businessName, ptvOrganization.getBusinessName())) {
        continue;
      }
      
      OrganizationId organizationId = ptvIdFactory.createOrganizationId(ptvOrganization.getId());
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId != null) {
        Organization organization = ptvTranslator.translateOrganization(kuntaApiOrganizationId, ptvOrganization);
        if (organization != null) {
          result.add(organization);
        }
      }
    }
    
    return result;
  }

}
