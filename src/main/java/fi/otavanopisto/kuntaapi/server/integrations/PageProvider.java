package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
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

  /**
   * Finds organization page contents
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @return page contents or null of not found
   */
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId);

  /**
   * Lists images attached to the page
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @return list of images attached to the page
   */
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId);
  
  /**
   * Finds a page image
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @param attachmentId image id
   * @return an page image or null if not found
   */
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId);
  
  /**
   * Returns data of page image
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @param attachmentId image id
   * @param size max size of image. Specify null for untouched
   * @return page image data
   */
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId, Integer size);
  
  
}
