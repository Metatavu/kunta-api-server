package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsRouteEntityTask extends AbstractGtfsEntityTask<Route> {
  
  private static final long serialVersionUID = -7567441890411767678L;
  
  private OrganizationId organizationId;
  private List<ServiceCalendar> serviceCalendars;
  
  public GtfsRouteEntityTask(OrganizationId organizationId, Route route, List<ServiceCalendar> serviceCalendars, Long orderIndex) {
    super(route, orderIndex);
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
  public Object[] getHashParts() {
    return new Object[] { organizationId, getEntity().getId() };
  }
  
  @Override
  public int getTaskHashInitialOddNumber() {
    return 1201;
  }
  
  @Override
  public int getMultiplierOddNumber() {
    return 1203;
  }

}