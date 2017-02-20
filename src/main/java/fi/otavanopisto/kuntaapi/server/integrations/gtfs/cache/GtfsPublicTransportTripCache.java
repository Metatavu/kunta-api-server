package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportTripCache;

@ApplicationScoped
public class GtfsPublicTransportTripCache extends AbstractPublicTransportTripCache {

  private static final long serialVersionUID = 8900780864580591473L;

  @Override
  public String getCacheName() {
    return "gtfs-public-transport-trips";
  }

}
