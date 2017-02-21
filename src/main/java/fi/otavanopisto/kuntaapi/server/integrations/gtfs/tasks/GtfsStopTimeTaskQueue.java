package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsStopTimeTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsStopTimeEntityTask> {

  @Override
  public String getEntityType() {
    return "stop-time";
  }

}
