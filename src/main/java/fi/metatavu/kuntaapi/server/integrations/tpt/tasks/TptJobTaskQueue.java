package fi.metatavu.kuntaapi.server.integrations.tpt.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

/**
 * Task queue for running te-palvelut.fi -integration job tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptJobTaskQueue extends AbstractJmsTaskQueue<TptAbstractJobTask> {
  
  public static final String NAME = "tpt-jobs";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }
  
}