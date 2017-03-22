package fi.otavanopisto.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public abstract class AbstractOrganizationResourceContainer extends AbstractResourceContainer<OrganizationId, Organization> {

  private static final long serialVersionUID = -7157857723713616445L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
