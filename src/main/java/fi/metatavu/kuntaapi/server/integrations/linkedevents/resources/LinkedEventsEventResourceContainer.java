package fi.metatavu.kuntaapi.server.integrations.linkedevents.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractEventResourceContainer;

@ApplicationScoped
public class LinkedEventsEventResourceContainer extends AbstractEventResourceContainer {
  
  private static final long serialVersionUID = -9084230289264356760L;

  @Override
  public String getName() {
    return "linkedevents";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
