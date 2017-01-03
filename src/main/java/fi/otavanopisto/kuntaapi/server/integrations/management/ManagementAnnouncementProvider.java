package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.AnnouncementCache;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;

/**
 * Announcement provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementProvider extends AbstractManagementProvider implements AnnouncementProvider {
  
  @Inject
  private AnnouncementCache announcementCache;
  
  @Override
  public List<Announcement> listOrganizationAnnouncements(OrganizationId organizationId) {
    List<AnnouncementId> announcementIds = announcementCache.getOragnizationIds(organizationId);
    List<Announcement> announcements = new ArrayList<>(announcementIds.size());
    
    for (AnnouncementId announcementId : announcementIds) {
      Announcement announcement = announcementCache.get(announcementId);
      if (announcement != null) {
        announcements.add(announcement);
      }
    }
    
    return announcements;
  }

  @Override
  public Announcement findOrganizationAnnouncement(OrganizationId organizationId, AnnouncementId announcementId) {
    return announcementCache.get(announcementId);
  }

}
