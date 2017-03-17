package fi.otavanopisto.kuntaapi.server.integrations.gtfs.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPublicTransportRouteResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportRouteResourceContainer extends AbstractPublicTransportRouteResourceContainer {

  private static final long serialVersionUID = -1804472147095708248L;

  @Override
  public String getName() {
    return "gtfs-public-transport-routes";
  }

}
