package fi.metatavu.kuntaapi.server.integrations.gtfs.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractPublicTransportTripResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportTripResourceContainer extends AbstractPublicTransportTripResourceContainer {

  private static final long serialVersionUID = 8900780864580591473L;

  @Override
  public String getName() {
    return "gtfs-public-transport-trips";
  }

}