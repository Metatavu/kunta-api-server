package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportRouteCache;

@ApplicationScoped
public class GtfsPublicTransportRouteCache extends AbstractPublicTransportRouteCache {

  private static final long serialVersionUID = -1804472147095708248L;

  @Override
  public String getName() {
    return "gtfs-public-transport-routes";
  }

}
