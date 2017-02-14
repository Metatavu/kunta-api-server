package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.IdMapProvider.OrganizationPageMap;

@ApplicationScoped
public abstract class AbstractPageIdMapCache extends AbstractEntityCache<OrganizationId, OrganizationPageMap> {

  private static final long serialVersionUID = -5473625018941962975L;
  
}
