package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.rest.model.Event;

@ApplicationScoped
public class EventCache extends AbstractEntityCache<EventId, Event> {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Override
  public String getCacheName() {
    return "events";
  }
  
}
