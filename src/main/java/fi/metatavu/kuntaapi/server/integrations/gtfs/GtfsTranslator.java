package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;

import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.rest.model.ScheduleException;
import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.metatavu.kuntaapi.server.rest.model.StopTime;
import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportAgencyId;
import fi.metatavu.kuntaapi.server.id.PublicTransportRouteId;
import fi.metatavu.kuntaapi.server.id.PublicTransportScheduleId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.metatavu.kuntaapi.server.id.PublicTransportTripId;
import fi.metatavu.kuntaapi.server.integrations.PublicTransportScheduleExceptionType;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

@ApplicationScoped
public class GtfsTranslator {

  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public Agency translateAgency(PublicTransportAgencyId kuntaApiId, org.onebusaway.gtfs.model.Agency agency) {
    Agency result = new Agency();
    result.setId(kuntaApiId.getId());
    result.setName(agency.getName());
    result.setUrl(agency.getUrl());
    result.setTimezone(agency.getTimezone());
   
    return result;
  }
  
  public Schedule translateSchedule(PublicTransportScheduleId kuntaApiId, ServiceCalendar serviceCalendar, List<ServiceCalendarDate> exceptions) {
    TimeZone organizationGtfsTimeZone = getOrganizationTimeZone(kuntaApiId.getOrganizationId());
    if(organizationGtfsTimeZone == null) {
      return null;
    }
    
    Schedule result = new Schedule();
    result.setId(kuntaApiId.getId());
    result.setName(serviceCalendar.getServiceId().getId());
    result.setDays(parseScheduleDays(serviceCalendar));
    result.setStartDate(parseServiceDateTime(serviceCalendar.getStartDate(), organizationGtfsTimeZone));
    result.setEndDate(parseServiceDateTime(serviceCalendar.getEndDate(), organizationGtfsTimeZone));
    result.setExceptions(parseScheduleExceptions(exceptions, organizationGtfsTimeZone));
    
    return result;
    
  }
  
  public Route translateRoute(PublicTransportRouteId kuntaApiId, org.onebusaway.gtfs.model.Route gtfsRoute, PublicTransportAgencyId kuntaApiAgencyId) {
    Route result = new Route();
    result.setId(kuntaApiId.getId());
    result.setAgencyId(kuntaApiAgencyId.getId());
    result.setShortName(gtfsRoute.getShortName());
    result.setLongName(gtfsRoute.getLongName());
    
    return result;
  }

  public Stop translateStop(PublicTransportStopId kuntaApiId, org.onebusaway.gtfs.model.Stop gtfsStop) {
    Stop result = new Stop();
    result.setId(kuntaApiId.getId());
    result.setName(gtfsStop.getName());
    result.setLat(gtfsStop.getLat());
    result.setLng(gtfsStop.getLon());
    
    return result;
  }

  public StopTime translateStopTime(PublicTransportStopTimeId kuntaApiId, org.onebusaway.gtfs.model.StopTime gtfsStopTime, PublicTransportStopId kuntaApiStopId, PublicTransportTripId kuntaApiTripId) {
    StopTime result = new StopTime();
    result.setId(kuntaApiId.getId());
    result.setDepartureTime(gtfsStopTime.getDepartureTime());
    result.setArrivalTime(gtfsStopTime.getArrivalTime());
    result.setStopId(kuntaApiStopId.getId());
    result.setSequency(gtfsStopTime.getStopSequence());
    result.setDistanceTraveled(gtfsStopTime.getShapeDistTraveled());
    result.setTripId(kuntaApiTripId.getId());
    
    return result;
  }
  
  public Trip translateTrip(PublicTransportTripId kuntaApiId, org.onebusaway.gtfs.model.Trip gtfsTrip, PublicTransportRouteId kuntaApiRouteId, PublicTransportScheduleId kuntaApiScheduleId) {
    Trip result = new Trip();
    result.setId(kuntaApiId.getId());
    result.setRouteId(kuntaApiRouteId.getId());
    result.setScheduleId(kuntaApiScheduleId.getId());
    result.setHeadsign(gtfsTrip.getTripHeadsign());
    
    return result;
  }
  
  private TimeZone getOrganizationTimeZone(OrganizationId organizationId) {
    String timeZoneString = organizationSettingController.getSettingValue(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE);
    if(timeZoneString == null) {
      logger.log(Level.SEVERE, () -> String.format("Missing organization gtfs timezone setting for organization %s", organizationId));
      return null;
    }
    
    TimeZone result = TimeZone.getTimeZone(timeZoneString);
    if(result == null) {
      logger.log(Level.SEVERE, () -> String.format("Malformed organization gtfs timezone setting for organization %s", organizationId));
      return null;
    }
    
    return result;
  }
  
  private List<ScheduleException> parseScheduleExceptions(List<ServiceCalendarDate> exceptions, TimeZone organizationGtfsTimeZone) {
    List<ScheduleException> results = new ArrayList<>(exceptions.size());
    for(ServiceCalendarDate exception : exceptions) {
      results.add(parseScheduleException(exception, organizationGtfsTimeZone));
    }
    
    return results;
  }
  
  private ScheduleException parseScheduleException(ServiceCalendarDate exception, TimeZone organizationGtfsTimeZone) {
    ScheduleException result = new ScheduleException();
    result.setDate(parseServiceDateTime(exception.getDate(), organizationGtfsTimeZone));
    result.setType(parseScheduleExceptionType(exception.getExceptionType()));
    return result;
  }
  
  private String parseScheduleExceptionType(int type){
    if (type == ServiceCalendarDate.EXCEPTION_TYPE_ADD) {
      return PublicTransportScheduleExceptionType.ADD.name();
    } else if (type == ServiceCalendarDate.EXCEPTION_TYPE_REMOVE) {
      return PublicTransportScheduleExceptionType.REMOVE.name();
    }
    
    return null;
  }
  
  private OffsetDateTime parseServiceDateTime(ServiceDate serviceDate, TimeZone timeZone) {
    if(serviceDate == null) {
      return null;
    }
    
    Calendar serviceCalendar = serviceDate.getAsCalendar(timeZone);
    
    if(serviceCalendar == null) {
      return null;
    }
    
    return serviceCalendar.toInstant().atOffset(ZoneOffset.UTC);
  }
  
  private List<Integer> parseScheduleDays(ServiceCalendar serviceCalendar) {
    List<Integer> days = new ArrayList<>(7);
    
    if (serviceCalendar.getMonday() > 0) {
      days.add(1);
    }
    
    if (serviceCalendar.getTuesday() > 0) {
      days.add(2);
    }
    
    if (serviceCalendar.getWednesday() > 0) {
      days.add(3);
    }
    
    if (serviceCalendar.getThursday() > 0) {
      days.add(4);
    }
    
    if (serviceCalendar.getFriday() > 0) {
      days.add(5);
    }
    
    if (serviceCalendar.getSaturday() > 0) {
      days.add(6);
    }
    
    if (serviceCalendar.getSunday() > 0) {
      days.add(0);
    }
    return days;
  }
  
}
