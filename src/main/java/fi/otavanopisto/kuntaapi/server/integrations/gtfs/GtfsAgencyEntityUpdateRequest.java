package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.Agency;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsAgencyEntityUpdateRequest extends AbstractEntityUpdateRequest<Agency> {
  
  private OrganizationId organizationId;
  
  public GtfsAgencyEntityUpdateRequest(OrganizationId organizationId, Agency agency, Long orderIndex, boolean priority) {
    super(agency, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsAgencyEntityUpdateRequest) {
      GtfsAgencyEntityUpdateRequest another = (GtfsAgencyEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1193, 1195)
      .append(getEntity().getId())
      .hashCode();
  }
}
