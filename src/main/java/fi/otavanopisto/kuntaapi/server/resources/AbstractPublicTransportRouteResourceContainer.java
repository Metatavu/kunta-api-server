package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;

@ApplicationScoped
public abstract class AbstractPublicTransportRouteResourceContainer extends AbstractResourceContainer<PublicTransportRouteId, Route> {

  private static final long serialVersionUID = -4071835963236525677L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}