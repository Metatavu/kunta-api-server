package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.Trip;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsTripEntityUpdateRequest extends AbstractEntityUpdateRequest<Trip> {
  
  private OrganizationId organizationId;
  
  public GtfsTripEntityUpdateRequest(OrganizationId organizationId, Trip trip, Long orderIndex, boolean priority) {
    super(trip, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsTripEntityUpdateRequest) {
      GtfsTripEntityUpdateRequest another = (GtfsTripEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1303, 1305)
      .append(getEntity().getId())
      .hashCode();
  }
}