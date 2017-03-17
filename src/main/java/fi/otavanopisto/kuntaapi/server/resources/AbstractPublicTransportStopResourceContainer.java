package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;

@ApplicationScoped
public abstract class AbstractPublicTransportStopResourceContainer extends AbstractResourceContainer<PublicTransportStopId, Stop> {

  private static final long serialVersionUID = 5118259973444207732L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}