package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsRouteTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsRouteEntityTask> {

  @Override
  public String getEntityType() {
    return "route";
  }

}
