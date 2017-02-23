package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsTripTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsTripEntityTask> {

  @Override
  public String getEntityType() {
    return "trip";
  }

}
