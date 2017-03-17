package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportTripResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportTripCache extends AbstractPublicTransportTripResourceContainer {

  private static final long serialVersionUID = 8900780864580591473L;

  @Override
  public String getName() {
    return "gtfs-public-transport-trips";
  }

}
