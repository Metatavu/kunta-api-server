package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractPublicTransportStopCache extends AbstractResourceContainer<PublicTransportStopId, Stop> {

  private static final long serialVersionUID = 5118259973444207732L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}