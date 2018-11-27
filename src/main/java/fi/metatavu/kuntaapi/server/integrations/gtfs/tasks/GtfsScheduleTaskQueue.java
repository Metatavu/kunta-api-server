package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class GtfsScheduleTaskQueue extends AbstractJmsTaskQueue<GtfsScheduleEntityTask> {
  
  public static final String NAME = "gtfs-schedules";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}