package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.DefaultJmsTaskQueue;

@ApplicationScoped
public class ServiceChannelTasksQueue extends DefaultJmsTaskQueue<AbstractServiceChannelTask> {

  public static final String NAME = "ptv-service-channels";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}