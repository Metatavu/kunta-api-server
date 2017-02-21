package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.Stop;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsStopEntityUpdateRequest extends AbstractEntityUpdateRequest<Stop> {
  
  private OrganizationId organizationId;
  
  public GtfsStopEntityUpdateRequest(OrganizationId organizationId, Stop stop, Long orderIndex, boolean priority) {
    super(stop, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsStopEntityUpdateRequest) {
      GtfsStopEntityUpdateRequest another = (GtfsStopEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1205, 1207)
      .append(getEntity().getId())
      .hashCode();
  }
}