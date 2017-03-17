package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportStopResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportStopCache extends AbstractPublicTransportStopResourceContainer {

  private static final long serialVersionUID = 1195621948823629420L;
  
  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

}
