package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsStopTimeTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsStopTimeEntityTask> {

  @Override
  public String getEntityType() {
    return "stop-time";
  }

}
