package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;

@ApplicationScoped
public abstract class AbstractPublicTransportAgencyResourceContainer extends AbstractResourceContainer<PublicTransportAgencyId, Agency> {

  private static final long serialVersionUID = -3896497752201194064L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}