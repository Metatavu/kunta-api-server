package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.rest.model.Event;

@ApplicationScoped
public abstract class AbstractEventResourceContainer extends AbstractResourceContainer<EventId, Event> {
  
  private static final long serialVersionUID = -26437859056938373L;

  @Override
  public String getEntityType() {
    return "resource";
  }

}
