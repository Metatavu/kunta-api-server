package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.rest.model.Page;

/**
 * Interafce that describes a single page provider
 * 
 * @author Antti Leppä
 */
public interface PageProvider {

  /**
   * List pages in an organization
   * 
   * @param organizationId organization id
   * @param filter results by parent id (optional)
   * @param filter results by path (optional)
   * @return list of organization pages
   */
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, String path);
  
  /**
   * Finds a single organization page
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @return page or null of not found
   */
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId);
  
}
