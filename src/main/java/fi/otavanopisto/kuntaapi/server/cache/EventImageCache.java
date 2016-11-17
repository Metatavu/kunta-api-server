package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;

@ApplicationScoped
public class EventImageCache extends AbstractEntityRelationCache<EventId, AttachmentId, Attachment> {
 
  private static final long serialVersionUID = -3586140702251269645L;

  @Override
  public String getCacheName() {
    return "event-images";
  }
  
}
