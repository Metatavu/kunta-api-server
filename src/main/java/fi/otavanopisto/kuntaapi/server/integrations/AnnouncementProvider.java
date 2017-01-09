package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;

/**
 * Interface that describes a single announcement provider
 * 
 * @author Antti Leppä
 */
public interface AnnouncementProvider {
  
  /**
   * Finds a single organization announcement
   * 
   * @param organizationId organization id
   * @param announcementId announcement id
   * @return single organization announcement or null if not found
   */
  public Announcement findOrganizationAnnouncement(OrganizationId organizationId, AnnouncementId announcementId);

  /**
   * Lists announcements in an organization
   * 
   * @param organizationId organization id
   * @param slug filter by slug
   * @return organization announcements
   */
  public List<Announcement> listOrganizationAnnouncements(OrganizationId organizationId, String slug);

  /**
   * Announcement order direction
   * 
   * @author Antti Leppä
   */
  public enum AnnouncementOrderDirection {
    
    ASCENDING,
    
    DESCENDING
  }
  
  /**
   * Announcement order
   * 
   * @author Antti Leppä
   */
  public enum AnnouncementOrder {
    
    PUBLICATION_DATE
    
  }

  
  
}
