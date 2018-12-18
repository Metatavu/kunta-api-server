package fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class KuntaRekryJobTaskQueue extends AbstractJmsTaskQueue<AbstractKuntaRekryJobTask> {
  
  public static final String NAME = "kunta-rekry-jobs";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }
  
}
