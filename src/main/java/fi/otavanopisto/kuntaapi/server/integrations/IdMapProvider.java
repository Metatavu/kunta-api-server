package fi.otavanopisto.kuntaapi.server.integrations;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

/**
 * Interface that describes a id mapping provider
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1609")
public interface IdMapProvider {
  
  /**
   * Finds a mapped parent id for the given page id
   * 
   * @param organizationId organization id
   * @param pageId page id
   * @return mapped id or null if page is not mapped
   */
  public BaseId findMappedPageParentId(OrganizationId organizationId, PageId pageId);
  
  
}
