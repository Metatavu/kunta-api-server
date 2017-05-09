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
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Organization;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class OrganizationController {

  @Inject
  private EntityController entityController;
  
  @Inject
  private OrganizationSearcher organizationSearcher;
  
  @Inject
  private Instance<OrganizationProvider> organizationProviders;
  
  public List<Organization> listOrganizations(Long firstResult, Long maxResults) {
    List<Organization> organizations = new ArrayList<>();
    
    for (OrganizationProvider organizationProvider : getOrganizationProviders()) {
      organizations.addAll(organizationProvider.listOrganizations(null, null));
    }
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(organizations), firstResult, maxResults);
  }

  public SearchResult<Organization> searchOrganizations(String search, String businessName, String businessCode, Long firstResult, Long maxResults) {
    SearchResult<OrganizationId> searchResult;
    
    if (search == null) {
      searchResult = searchByBusinessNameOrBusinessCode(businessName, businessCode, firstResult, maxResults);
      if (searchResult == null) {
        // Search has failed, fall back to listing
        List<Organization> organizations = listByBusinessNameOrBusinessCode(businessName, businessCode);
        return new SearchResult<>(organizations, organizations.size());
      }
    } else {
      searchResult = organizationSearcher.searchOrganizations(search, businessCode, businessName, firstResult, maxResults);
    }
    
    if (searchResult != null) {
      List<Organization> result = new ArrayList<>();
      
      for (OrganizationId organizationId : searchResult.getResult()) {
        Organization organization = findOrganization(organizationId);
        if (organization != null) {
          result.add(organization);
        }
      }
      
      return new SearchResult<>(result, searchResult.getTotalHits());
    }
    
    return SearchResult.emptyResult();
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
