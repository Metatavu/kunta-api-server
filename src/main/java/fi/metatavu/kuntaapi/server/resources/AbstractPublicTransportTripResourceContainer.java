package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.metatavu.kuntaapi.server.id.PublicTransportTripId;

@ApplicationScoped
public abstract class AbstractPublicTransportTripResourceContainer extends AbstractResourceContainer<PublicTransportTripId, Trip> {

  private static final long serialVersionUID = -3988560340679373175L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}