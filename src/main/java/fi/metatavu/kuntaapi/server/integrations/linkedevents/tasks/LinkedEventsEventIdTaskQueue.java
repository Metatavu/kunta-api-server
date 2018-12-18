package fi.metatavu.kuntaapi.server.integrations.linkedevents.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractIdTaskQueue;

@ApplicationScoped
public class LinkedEventsEventIdTaskQueue extends AbstractIdTaskQueue<EventId> {

  @Override
  public String getSource() {
    return LinkedEventsConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.EVENT;
  }
  
}