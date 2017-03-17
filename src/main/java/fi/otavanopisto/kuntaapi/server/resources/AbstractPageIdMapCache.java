package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.IdMapProvider.OrganizationPageMap;

@ApplicationScoped
public abstract class AbstractPageIdMapCache extends AbstractResourceContainer<OrganizationId, OrganizationPageMap> {

  private static final long serialVersionUID = -5473625018941962975L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
