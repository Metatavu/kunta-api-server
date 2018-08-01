package fi.metatavu.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Shortlink;

/**
 * Interface that describes a single shortlink provider
 * 
 * @author Antti Leppä
 */
public interface ShortlinkProvider {
  
  /**
   * Finds a single organization shortlink
   * 
   * @param organizationId organization id
   * @param shortlinkId shortlink id
   * @return single organization shortlink or null if not found
   */
  public Shortlink findOrganizationShortlink(OrganizationId organizationId, ShortlinkId shortlinkId);

  /**
   * Lists shortlinks in an organization
   * 
   * @param organizationId organization id
   * @param path path
   * @return organization shortlinks
   */
  public List<Shortlink> listOrganizationShortlinks(OrganizationId organizationId, String path);
  
}