package fi.otavanopisto.kuntaapi.server.integrations.gtfs.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportScheduleResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportScheduleResourceContainer extends AbstractPublicTransportScheduleResourceContainer {

  private static final long serialVersionUID = -5861987244184893788L;

  @Override
  public String getName() {
    return "gtfs-public-transport-schedules";
  }

}
