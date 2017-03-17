package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

public abstract class AbstractOrganizationServiceCache extends AbstractResourceContainer<OrganizationServiceId, OrganizationService> {

  private static final long serialVersionUID = -5629361755545994677L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
