package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class OrganizationIdTaskQueue extends AbstractJmsTaskQueue<IdTask<OrganizationId>> {
  
  public static final String NAME = "ptv-organizations";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }
  
}