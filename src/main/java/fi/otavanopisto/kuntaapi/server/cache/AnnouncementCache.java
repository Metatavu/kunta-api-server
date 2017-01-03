package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;

@ApplicationScoped
public class AnnouncementCache extends AbstractEntityCache<AnnouncementId, Announcement> {

  private static final long serialVersionUID = -4405052382337717452L;

  @Override
  public String getCacheName() {
    return "announcements";
  }
  
}