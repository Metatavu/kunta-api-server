package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.HashMap;

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
  
  public static class OrganizationPageMap extends HashMap<PageId, BaseId> {

    private static final long serialVersionUID = 6317362026173577227L;
    
  }
  
}
