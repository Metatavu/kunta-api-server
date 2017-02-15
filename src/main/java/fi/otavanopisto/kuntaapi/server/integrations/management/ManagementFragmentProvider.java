package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Fragment;
import fi.otavanopisto.kuntaapi.server.cache.FragmentCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.FragmentProvider;

/**
 * Fragment provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementFragmentProvider extends AbstractManagementProvider implements FragmentProvider {
  
  @Inject
  private FragmentCache fragmentCache;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public List<Fragment> listOrganizationFragments(OrganizationId organizationId, String slug) {
    List<FragmentId> fragmentIds = identifierRelationController.listFragmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Fragment> fragments = new ArrayList<>(fragmentIds.size());
    
    for (FragmentId fragmentId : fragmentIds) {
      Fragment fragment = fragmentCache.get(fragmentId);
      if (fragment != null && isAcceptable(fragment, slug)) {
        fragments.add(fragment);
      }
    }
    
    return fragments;
  }

  @Override
  public Fragment findOrganizationFragment(OrganizationId organizationId, FragmentId fragmentId) {
    return fragmentCache.get(fragmentId);
  }

  private boolean isAcceptable(Fragment fragment, String slug) {
    if (slug == null) {
      return true;
    }
    
    return StringUtils.equals(slug, fragment.getSlug());
  }
  
}
