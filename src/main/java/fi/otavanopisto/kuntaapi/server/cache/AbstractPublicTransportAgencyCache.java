package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractPublicTransportAgencyCache extends AbstractResourceContainer<PublicTransportAgencyId, Agency> {

  private static final long serialVersionUID = -3896497752201194064L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}