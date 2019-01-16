package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.DefaultJmsTaskQueue;

@ApplicationScoped
public class GtfsAgencyTaskQueue extends DefaultJmsTaskQueue<GtfsAgencyEntityTask> {
  
  public static final String NAME = "gtfs-agencies";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}
