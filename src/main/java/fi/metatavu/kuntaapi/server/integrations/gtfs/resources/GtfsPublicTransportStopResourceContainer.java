package fi.metatavu.kuntaapi.server.integrations.gtfs.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractPublicTransportStopResourceContainer;

@ApplicationScoped
public class GtfsPublicTransportStopResourceContainer extends AbstractPublicTransportStopResourceContainer {

  private static final long serialVersionUID = 1195621948823629420L;
  
  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

}