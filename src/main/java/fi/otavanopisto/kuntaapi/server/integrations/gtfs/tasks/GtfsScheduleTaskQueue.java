package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

public class GtfsScheduleTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsScheduleEntityTask> {

  @Override
  public String getEntityType() {
    return "schedule";
  }

}
