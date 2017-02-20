package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onebusaway.gtfs.model.Route;

import fi.otavanopisto.kuntaapi.server.discover.AbstractEntityUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import java.util.List;
import org.onebusaway.gtfs.model.ServiceCalendar;

public class GtfsRouteEntityUpdateRequest extends AbstractEntityUpdateRequest<Route> {
  
  private OrganizationId organizationId;
  private List<ServiceCalendar> serviceCalendars;
  
  public GtfsRouteEntityUpdateRequest(OrganizationId organizationId, Route route, List<ServiceCalendar> serviceCalendars, Long orderIndex, boolean priority) {
    super(route, orderIndex, priority);
    this.organizationId = organizationId;
    this.serviceCalendars = serviceCalendars;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }

  public List<ServiceCalendar> getServiceCalendars() {
    return serviceCalendars;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GtfsRouteEntityUpdateRequest) {
      GtfsRouteEntityUpdateRequest another = (GtfsRouteEntityUpdateRequest) obj;
      return another.getEntity().getId().equals(this.getEntity().getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1201, 1203)
      .append(getEntity().getId())
      .hashCode();
  }
}