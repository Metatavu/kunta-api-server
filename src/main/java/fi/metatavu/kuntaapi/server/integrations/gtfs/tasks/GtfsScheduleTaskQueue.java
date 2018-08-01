package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsScheduleTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsScheduleEntityTask> {

  @Override
  public String getEntityType() {
    return "schedule";
  }

}
