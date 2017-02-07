package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;

/**
 * Interface that describes a single page provider
 * 
 * @author Antti Leppä
 */
public interface PageProvider {

  /**
   * List pages in an organization
   * 
   * @param organizationId organization id
   * @param filter results by parent id (optional)
   * @param return only root pages. When this is set to true, parentId parameter is ignored
   * @return list of organization pages
   */
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages);
  
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
   * @param type image type
   * @return list of images attached to the page
   */
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId, String type);
  
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
