package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportAgencyCache;

@ApplicationScoped
public class GtfsPublicTransportAgencyCache extends AbstractPublicTransportAgencyCache {

  private static final long serialVersionUID = 5885760951047040901L;

  @Override
  public String getCacheName() {
    return "gtfs-public-transport-agencies";
  }

}
