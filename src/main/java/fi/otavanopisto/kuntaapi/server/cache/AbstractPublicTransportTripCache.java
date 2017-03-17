package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractPublicTransportTripCache extends AbstractResourceContainer<PublicTransportTripId, Trip> {

  private static final long serialVersionUID = -3988560340679373175L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}