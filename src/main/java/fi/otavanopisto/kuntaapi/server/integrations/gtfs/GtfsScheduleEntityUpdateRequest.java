package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsScheduleEntityUpdateRequest extends AbstractEntityUpdateRequest<ServiceCalendar> {
  
  private OrganizationId organizationId;
  private List<ServiceCalendarDate> exceptions;
  
  public GtfsScheduleEntityUpdateRequest(OrganizationId organizationId, ServiceCalendar serviceCalendar, List<ServiceCalendarDate> exceptions, Long orderIndex, boolean priority) {
    super(serviceCalendar, orderIndex, priority);
    this.organizationId = organizationId;
    this.exceptions = exceptions;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public List<ServiceCalendarDate> getExceptions() {
    return exceptions;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsScheduleEntityUpdateRequest) {
      GtfsScheduleEntityUpdateRequest another = (GtfsScheduleEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1197, 1199)
      .append(getEntity().getId())
      .hashCode();
  }
}
