package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.OrganizationSearcher;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;

@ApplicationScoped
public class OrganizationController {
  
  @Inject
  private OrganizationSearcher organizationSearcher;
  
  @Inject
  private Instance<OrganizationProvider> organizationProviders;
  
  public List<Organization> listOrganizations(String businessName, String businessCode, Long firstResult, Long maxResults) {
    List<Organization> organizations = new ArrayList<>();
    
    if (businessName != null || businessCode != null) {
      organizations = searchOrganizations(null, businessName, businessCode, firstResult, maxResults);
    } else {
      for (OrganizationProvider organizationProvider : getOrganizationProviders()) {
        organizations.addAll(organizationProvider.listOrganizations(businessName, businessCode));
      }
    }
    
    return organizations;
  }

  public List<Organization> searchOrganizations(String search, String businessName, String businessCode, Long firstResult, Long maxResults) {
    List<Organization> result = new ArrayList<>();
    SearchResult<OrganizationId> searchResult;
    
    if (search == null) {
      searchResult = searchByBusinessNameOrBusinessCode(businessName, businessCode, firstResult, maxResults);
      if (searchResult == null) {
        // Search has failed, fall back to listing
        return listByBusinessNameOrBusinessCode(businessName, businessCode);
      }
    } else {
      searchResult = organizationSearcher.searchOrganizations(search, businessCode, businessName, firstResult, maxResults);
    }
    
    if (searchResult != null) {
      for (OrganizationId organizationId : searchResult.getResult()) {
        Organization organization = findOrganization(organizationId);
        if (organization != null) {
          result.add(organization);
        }
      }
    }
    
    return result;
  }

  private SearchResult<OrganizationId> searchByBusinessNameOrBusinessCode(String businessName, String businessCode, Long firstResult, Long maxResults) {
    if (businessName == null && businessCode != null) {
      return organizationSearcher.searchOrganizationsByBusinessCode(businessCode, firstResult, maxResults);
    } else if (businessName != null && businessCode == null) {
      return organizationSearcher.searchOrganizationsByBusinessName(businessName, firstResult, maxResults);
    } else {
      return organizationSearcher.searchOrganizationsByBusinessCodeAndBusinessName(businessCode, businessName, firstResult, maxResults);
    }
  }

  private List<Organization> listByBusinessNameOrBusinessCode(String businessName, String businessCode) {
    for (OrganizationProvider organizationProvider : getOrganizationProviders()) {
      List<Organization> organizations = organizationProvider.listOrganizations(businessName, businessCode);
      if (organizations != null) {
        return organizations;
      }
    }
    
    return Collections.emptyList();
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
