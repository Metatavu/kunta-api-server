package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsStopTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsStopEntityTask> {

  @Override
  public String getEntityType() {
    return "stop";
  }

}
