package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;

/**
 * Interface that describes a single fragment provider
 * 
 * @author Antti Leppä
 */
public interface FragmentProvider {
  
  /**
   * Finds a single organization fragment
   * 
   * @param organizationId organization id
   * @param fragmentId fragment id
   * @return single organization fragment or null if not found
   */
  public Fragment findOrganizationFragment(OrganizationId organizationId, FragmentId fragmentId);

  /**
   * Lists fragments in an organization
   * 
   * @param organizationId organization id
   * @param slug slug
   * @return organization fragments
   */
  public List<Fragment> listOrganizationFragments(OrganizationId organizationId, String slug);
  
}