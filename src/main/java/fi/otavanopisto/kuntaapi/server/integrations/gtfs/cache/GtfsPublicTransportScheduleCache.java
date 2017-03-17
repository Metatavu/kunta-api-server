package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportScheduleResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportScheduleCache extends AbstractPublicTransportScheduleResourceContainer {

  private static final long serialVersionUID = -5861987244184893788L;

  @Override
  public String getName() {
    return "gtfs-public-transport-schedules";
  }

}
