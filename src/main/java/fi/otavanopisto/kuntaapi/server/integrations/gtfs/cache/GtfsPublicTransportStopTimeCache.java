package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportStopTimeResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportStopTimeCache extends AbstractPublicTransportStopTimeResourceContainer {

  private static final long serialVersionUID = -889639898690519914L;

  @Override
  public String getName() {
    return "gtfs-public-transport-stoptimes";
  }

}
