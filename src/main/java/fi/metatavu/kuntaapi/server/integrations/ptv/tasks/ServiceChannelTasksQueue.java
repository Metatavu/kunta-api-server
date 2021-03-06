package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class ServiceChannelTasksQueue extends AbstractJmsTaskQueue<AbstractServiceChannelTask, Serializable> {

  public static final String NAME = "ptv-service-channels";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}