package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsRouteTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsRouteEntityTask> {

  @Override
  public String getEntityType() {
    return "route";
  }

}
