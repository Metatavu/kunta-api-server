package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPublicTransportScheduleCache;

@ApplicationScoped
public class GtfsPublicTransportScheduleCache extends AbstractPublicTransportScheduleCache {

  private static final long serialVersionUID = -5861987244184893788L;

  @Override
  public String getCacheName() {
    return "gtfs-public-transport-schedules";
  }

}
