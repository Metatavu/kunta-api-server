package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.FragmentProvider;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class FragmentController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<FragmentProvider> fragmentProviders;
  
  public List<Fragment> listFragments(OrganizationId organizationId, String slug, Integer firstResult, Integer maxResults) {
    List<Fragment> result = new ArrayList<>();
   
    for (FragmentProvider fragmentProvider : getFragmentProviders()) {
      result.addAll(fragmentProvider.listOrganizationFragments(organizationId, slug));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }

  public Fragment findFragment(OrganizationId organizationId, FragmentId fragmentId) {
    for (FragmentProvider fragmentProvider : getFragmentProviders()) {
      Fragment fragment = fragmentProvider.findOrganizationFragment(organizationId, fragmentId);
      if (fragment != null) {
        return fragment;
      }
    }
    
    return null;
  }
  
  private List<FragmentProvider> getFragmentProviders() {
    List<FragmentProvider> result = new ArrayList<>();
    
    Iterator<FragmentProvider> iterator = fragmentProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}