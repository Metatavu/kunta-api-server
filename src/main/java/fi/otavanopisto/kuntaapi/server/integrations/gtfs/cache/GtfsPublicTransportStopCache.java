package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportStopCache;

@ApplicationScoped
public class GtfsPublicTransportStopCache extends AbstractPublicTransportStopCache {

  private static final long serialVersionUID = 1195621948823629420L;
  
  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

}
