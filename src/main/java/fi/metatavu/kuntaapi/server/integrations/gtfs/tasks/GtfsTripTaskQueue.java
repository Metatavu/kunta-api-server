package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.DefaultJmsTaskQueue;

@ApplicationScoped
public class GtfsTripTaskQueue extends DefaultJmsTaskQueue<GtfsTripEntityTask> {
  
  public static final String NAME = "gtfs-trips";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}