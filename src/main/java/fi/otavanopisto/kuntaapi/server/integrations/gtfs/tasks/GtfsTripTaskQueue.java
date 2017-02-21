package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsTripTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsTripEntityTask> {

  @Override
  public String getEntityType() {
    return "trip";
  }

}
