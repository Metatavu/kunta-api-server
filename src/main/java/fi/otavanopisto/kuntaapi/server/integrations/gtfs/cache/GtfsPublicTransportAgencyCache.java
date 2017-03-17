package fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportAgencyResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportAgencyCache extends AbstractPublicTransportAgencyResourceContainer {

  private static final long serialVersionUID = 5885760951047040901L;

  @Override
  public String getName() {
    return "gtfs-public-transport-agencies";
  }

}
