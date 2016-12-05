package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class OrganizationIdUpdateRequest extends AbstractIdUpdateRequest<OrganizationId> {

  public OrganizationIdUpdateRequest(OrganizationId id, boolean priority) {
    super(id, priority);
  }

}
