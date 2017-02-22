package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import java.util.List;

import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsScheduleEntityTask extends AbstractGtfsEntityTask<ServiceCalendar> {
  
  private static final long serialVersionUID = 3029667622376021093L;
  
  private OrganizationId organizationId;
  private List<ServiceCalendarDate> exceptions;
  
  public GtfsScheduleEntityTask(OrganizationId organizationId, ServiceCalendar serviceCalendar, List<ServiceCalendarDate> exceptions, Long orderIndex) {
    super(serviceCalendar, orderIndex);
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
  public Object[] getHashParts() {
    return new Object[] { organizationId, getEntity().getId() };
  }
  
  @Override
  public int getTaskHashInitialOddNumber() {
    return 1197;
  }
  
  @Override
  public int getMultiplierOddNumber() {
    return 1199;
  }

}
