package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.time.LocalDate;
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
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.rest.model.ScheduleException;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportScheduleExceptionType;

@ApplicationScoped
public class GtfsTranslator {

  public Agency translateAgency(PublicTransportAgencyId kuntaApiId, org.onebusaway.gtfs.model.Agency agency) {
    Agency result = new Agency();
    result.setId(kuntaApiId.getId());
    result.setName(agency.getName());
    result.setUrl(agency.getUrl());
    result.setTimezone(agency.getTimezone());
    return result;
  }
  
  public Schedule translateSchedule(PublicTransportScheduleId kuntaApiId, ServiceCalendar serviceCalendar, List<ServiceCalendarDate> exceptions) {
    Schedule result = new Schedule();
    result.setId(kuntaApiId.getId());
    result.setName(serviceCalendar.getServiceId().getId());
    result.setDays(parseScheduleDays(serviceCalendar));
    result.setStartDate(parseServiceDateTime(serviceCalendar.getStartDate()));
    result.setEndDate(parseServiceDateTime(serviceCalendar.getEndDate()));
    result.setExceptions(parseScheduleExceptions(exceptions));
    return result;
    
  }

  private List<ScheduleException> parseScheduleExceptions(List<ServiceCalendarDate> exceptions) {
    List<ScheduleException> results = new ArrayList<>(exceptions.size());
    for(ServiceCalendarDate exception : exceptions) {
      results.add(parseScheduleException(exception));
    }
    return results;
  }
  
  private ScheduleException parseScheduleException(ServiceCalendarDate exception) {
    ScheduleException result = new ScheduleException();
    result.setDate(parseServiceDate(exception.getDate()));
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
  
  private LocalDate parseServiceDate(ServiceDate serviceDate) {
    OffsetDateTime dateTime = parseServiceDateTime(serviceDate);
    if (dateTime == null) {
      return null;
    }
    
    return dateTime.toLocalDate();
  }
  
  private OffsetDateTime parseServiceDateTime(ServiceDate serviceDate) {
    if(serviceDate == null) {
      return null;
    }
    
    Calendar serviceCalendar = serviceDate.getAsCalendar(TimeZone.getDefault());
    
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
