package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.DefaultJmsTaskQueue;

@ApplicationScoped
public class GtfsStopTaskQueue extends DefaultJmsTaskQueue<GtfsStopEntityTask> {
  
  public static final String NAME = "gtfs-stops";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}