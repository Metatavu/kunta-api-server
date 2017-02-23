package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsStopTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsStopEntityTask> {

  @Override
  public String getEntityType() {
    return "stop";
  }

}
