package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;

@ApplicationScoped
public abstract class AbstractPublicTransportAgencyCache extends AbstractEntityCache<PublicTransportAgencyId, Agency> {

  private static final long serialVersionUID = -3896497752201194064L;
}