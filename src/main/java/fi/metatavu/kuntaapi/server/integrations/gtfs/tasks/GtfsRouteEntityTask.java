package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsRouteEntityTask extends AbstractGtfsEntityTask<Route> {
  
  private static final long serialVersionUID = -7567441890411767678L;
  
  private OrganizationId organizationId;
  private List<ServiceCalendar> serviceCalendars;
  
  public GtfsRouteEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsRouteEntityTask(boolean priority, OrganizationId organizationId, Route route, List<ServiceCalendar> serviceCalendars, Long orderIndex) {
    super(String.format("gtfs-route-entity-task-%s-%s", organizationId.toString(), route.getId()), priority, route, orderIndex);
    this.organizationId = organizationId;
    this.serviceCalendars = serviceCalendars;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }

  public List<ServiceCalendar> getServiceCalendars() {
    return serviceCalendars;
  }
  
}