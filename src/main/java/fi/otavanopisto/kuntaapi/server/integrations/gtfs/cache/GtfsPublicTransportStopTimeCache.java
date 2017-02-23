package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportStopTimeCache;

@ApplicationScoped
public class GtfsPublicTransportStopTimeCache extends AbstractPublicTransportStopTimeCache {

  private static final long serialVersionUID = -889639898690519914L;

  @Override
  public String getCacheName() {
    return "gtfs-public-transport-stoptimes";
  }

}