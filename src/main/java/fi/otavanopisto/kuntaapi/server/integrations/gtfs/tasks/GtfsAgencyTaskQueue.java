package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsAgencyTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsAgencyEntityTask> {

  @Override
  public String getEntityType() {
    return "agency";
  }

}
