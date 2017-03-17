package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;

@ApplicationScoped
public class AnnouncementCache extends AbstractResourceContainer<AnnouncementId, Announcement> {

  private static final long serialVersionUID = -4405052382337717452L;

  @Override
  public String getName() {
    return "announcements";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}