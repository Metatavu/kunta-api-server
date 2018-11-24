package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class GtfsRouteTaskQueue extends AbstractJmsTaskQueue<GtfsRouteEntityTask> {
  
  public static final String NAME = "gtfs-routes";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}