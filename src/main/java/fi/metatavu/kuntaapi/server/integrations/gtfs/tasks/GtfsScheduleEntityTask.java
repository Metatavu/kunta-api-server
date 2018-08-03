package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import java.util.List;

import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsScheduleEntityTask extends AbstractGtfsEntityTask<ServiceCalendar> {
  
  private static final long serialVersionUID = 3029667622376021093L;
  
  private OrganizationId organizationId;
  private List<ServiceCalendarDate> exceptions;
  
  public GtfsScheduleEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsScheduleEntityTask(boolean priority, OrganizationId organizationId, ServiceCalendar serviceCalendar, List<ServiceCalendarDate> exceptions, Long orderIndex) {
    super(String.format("gtfs-schedule-entity-task-%s-%s", organizationId.toString(), serviceCalendar.getId()), priority, serviceCalendar, orderIndex);
    this.organizationId = organizationId;
    this.exceptions = exceptions;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public List<ServiceCalendarDate> getExceptions() {
    return exceptions;
  }

}
