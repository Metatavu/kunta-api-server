package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.StopTime;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsStopTimeEntityUpdateRequest extends AbstractEntityUpdateRequest<StopTime> {
  
  private OrganizationId organizationId;
  
  public GtfsStopTimeEntityUpdateRequest(OrganizationId organizationId, StopTime stopTime, Long orderIndex, boolean priority) {
    super(stopTime, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsStopTimeEntityUpdateRequest) {
      GtfsStopTimeEntityUpdateRequest another = (GtfsStopTimeEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1209, 1301)
      .append(getEntity().getId())
      .hashCode();
  }
}