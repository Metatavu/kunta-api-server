package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrder;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrderDirection;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class AnnouncementController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<AnnouncementProvider> announcementProviders;
  
  public List<Announcement> listAnnouncements(OrganizationId organizationId, AnnouncementOrder order, AnnouncementOrderDirection orderDirection, Integer firstResult, Integer maxResults) {
    List<Announcement> result = new ArrayList<>();
   
    for (AnnouncementProvider announcementProvider : getAnnouncementProviders()) {
      result.addAll(announcementProvider.listOrganizationAnnouncements(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return sortAnnouncements(result.subList(firstIndex, toIndex), order, orderDirection);
  }
  
  @SuppressWarnings ("squid:S1301")
  private List<Announcement> sortAnnouncements(List<Announcement> announcements, AnnouncementOrder order, AnnouncementOrderDirection orderDirection) {
    if (order == null) {
      return entityController.sortEntitiesInNaturalOrder(announcements);
    }
    
    List<Announcement> sorted = new ArrayList<>(announcements);
    
    switch (order) {
      case PUBLICATION_DATE:
        Collections.sort(sorted, (Announcement o1, Announcement o2)
          -> orderDirection != AnnouncementOrderDirection.ASCENDING 
            ? o2.getPublished().compareTo(o1.getPublished())
            : o1.getPublished().compareTo(o2.getPublished()));
      break;
      default:
    }

    return sorted;
  }

  public Announcement findAnnouncement(OrganizationId organizationId, AnnouncementId announcementId) {
    for (AnnouncementProvider announcementProvider : getAnnouncementProviders()) {
      Announcement announcement = announcementProvider.findOrganizationAnnouncement(organizationId, announcementId);
      if (announcement != null) {
        return announcement;
      }
    }
    
    return null;
  }
  
  private List<AnnouncementProvider> getAnnouncementProviders() {
    List<AnnouncementProvider> result = new ArrayList<>();
    
    Iterator<AnnouncementProvider> iterator = announcementProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
