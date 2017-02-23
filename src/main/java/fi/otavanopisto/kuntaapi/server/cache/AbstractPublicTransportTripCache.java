package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;

@ApplicationScoped
public abstract class AbstractPublicTransportTripCache extends AbstractEntityCache<PublicTransportTripId, Trip> {

  private static final long serialVersionUID = -3988560340679373175L;
  
}