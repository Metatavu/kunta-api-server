package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GtfsAgencyTaskQueue extends AbstractGtfsEntityTaskQueue<GtfsAgencyEntityTask> {

  @Override
  public String getEntityType() {
    return "agency";
  }

}
