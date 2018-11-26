package fi.metatavu.kuntaapi.server.integrations.tilannehuone.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class TilannehuoneEmergencyTaskQueue extends AbstractJmsTaskQueue<TilannehuoneEmergencyEntityTask> {
  
  public static final String NAME = "tilannehuone-emergencies";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }
  
}