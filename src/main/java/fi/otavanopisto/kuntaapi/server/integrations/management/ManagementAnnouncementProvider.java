package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Announcement;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider;
import fi.otavanopisto.kuntaapi.server.resources.AnnouncementResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Announcement provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementProvider extends AbstractManagementProvider implements AnnouncementProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private AnnouncementResourceContainer announcementResourceContainer;
  
  @Override
  public List<Announcement> listOrganizationAnnouncements(OrganizationId organizationId, String slug) {
    List<AnnouncementId> announcementIds = identifierRelationController.listAnnouncementIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Announcement> announcements = new ArrayList<>(announcementIds.size());
    
    for (AnnouncementId announcementId : announcementIds) {
      Announcement announcement = announcementResourceContainer.get(announcementId);
      if (announcement != null && isAcceptable(announcement, slug)) {
        announcements.add(announcement);
      }
    }
    
    return announcements;
  }

  @Override
  public Announcement findOrganizationAnnouncement(OrganizationId organizationId, AnnouncementId announcementId) {
    if (identifierRelationController.isChildOf(organizationId, announcementId)) {
      return announcementResourceContainer.get(announcementId);
    }
    
    return null;
  }

  private boolean isAcceptable(Announcement announcement, String slug) {
    if (slug == null) {
      return true;
    }
    
    return StringUtils.equals(slug, announcement.getSlug());
  }
  
}
