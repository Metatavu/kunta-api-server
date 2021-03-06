package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Fragment;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.FragmentProvider;
import fi.metatavu.kuntaapi.server.resources.FragmentResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Fragment provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementFragmentProvider extends AbstractManagementProvider implements FragmentProvider {
  
  @Inject
  private FragmentResourceContainer fragmentResourceContainer;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public List<Fragment> listOrganizationFragments(OrganizationId organizationId, String slug) {
    List<FragmentId> fragmentIds = identifierRelationController.listFragmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Fragment> fragments = new ArrayList<>(fragmentIds.size());
    
    for (FragmentId fragmentId : fragmentIds) {
      Fragment fragment = fragmentResourceContainer.get(fragmentId);
      if (fragment != null && isAcceptable(fragment, slug)) {
        fragments.add(fragment);
      }
    }
    
    return fragments;
  }

  @Override
  public Fragment findOrganizationFragment(OrganizationId organizationId, FragmentId fragmentId) {
    if (identifierRelationController.isChildOf(organizationId, fragmentId)) {
      return fragmentResourceContainer.get(fragmentId);
    }
    
    return null;
  }

  private boolean isAcceptable(Fragment fragment, String slug) {
    if (slug == null) {
      return true;
    }
    
    return StringUtils.equals(slug, fragment.getSlug());
  }
  
}
