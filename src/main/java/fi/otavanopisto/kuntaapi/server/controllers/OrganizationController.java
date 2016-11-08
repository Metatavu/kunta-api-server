package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.OrganizationSearcher;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;

@Dependent
public class OrganizationController {
  
  @Inject
  private OrganizationSearcher organizationSearcher;
  
  @Inject
  private Instance<OrganizationProvider> organizationProviders;
  
  public List<Organization> listOrganizations(String businessName, String businessCode) {
    List<Organization> organizations = new ArrayList<>();
    
    if (businessName == null && businessCode != null) {
      SearchResult<OrganizationId> searchResult = organizationSearcher.searchOrganizationsByBusinessCode(businessCode);
      
      for (OrganizationId organizationId : searchResult.getResult()) {
        Organization organization = findOrganization(organizationId);
        if (organization != null) {
          organizations.add(organization);
        }
      }
    } else {
      for (OrganizationProvider organizationProvider : getOrganizationProviders()) {
        organizations.addAll(organizationProvider.listOrganizations(businessName, businessCode));
      }
    }
    
    return organizations;
  }

  public Organization findOrganization(OrganizationId organizationId) {
    for (OrganizationProvider organizationProvider : getOrganizationProviders()) {
      Organization organization = organizationProvider.findOrganization(organizationId);
      if (organization != null) {
        return organization;
      }
    }
    
    return null;
  }
  
  private List<OrganizationProvider> getOrganizationProviders() {
    List<OrganizationProvider> result = new ArrayList<>();
    
    Iterator<OrganizationProvider> iterator = organizationProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
