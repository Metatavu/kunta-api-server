package fi.metatavu.kuntaapi.server.integrations.linkedevents.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.metatavu.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationLinkedEventsEventTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return LinkedEventsConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "linkedevents";
  }
  
}