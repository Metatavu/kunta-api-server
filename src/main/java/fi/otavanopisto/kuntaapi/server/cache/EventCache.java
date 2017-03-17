package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Event;

@ApplicationScoped
public class EventCache extends AbstractResourceContainer<EventId, Event> {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Override
  public String getName() {
    return "events";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
