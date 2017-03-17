package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.StopTime;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;

@ApplicationScoped
public abstract class AbstractPublicTransportStopTimeResourceContainer extends AbstractResourceContainer<PublicTransportStopTimeId, StopTime> {

  private static final long serialVersionUID = 8435491545447129703L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}