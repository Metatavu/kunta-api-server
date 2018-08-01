package fi.metatavu.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
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

/**
 * Interface that describes a single public transport provider
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public interface PublicTransportProvider {
  
  /**
   * Lists agencies in an organization
   * 
   * @param organizationId organization id
   * @return organization agencies
   */
  public List<Agency> listAgencies(OrganizationId organizationId);
  
  
  /**
   * Finds single agency in an organization by id
   * 
   * @param organizationId organization id
   * @param agencyId agency id
   * @return agency or null if not found
   */
  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId);
  
  /**
   * Lists schedule in an organization
   * 
   * @param organizationId organization id
   * @return organization schedules
   */
  public List<Schedule> listSchedules(OrganizationId organizationId);
  
  /**
   * Finds single schedule in an organization by id
   * 
   * @param organizationId organization id
   * @param scheduleId schedule id
   * @return schedule or null if not found
   */
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId);
  
  /**
   * Lists route in an organization
   * 
   * @param organizationId organization id
   * @return organization routes
   */
  public List<Route> listRoutes(OrganizationId organizationId);
  
  /**
   * Finds single route in an organization by id
   * 
   * @param organizationId organization id
   * @param routeId route id
   * @return route or null if not found
   */
  public Route findRoute(OrganizationId organizationId, PublicTransportRouteId routeId);

  /**
   * Lists stop in an organization
   * 
   * @param organizationId organization id
   * @return organization stops
   */
  public List<Stop> listStops(OrganizationId organizationId);
  
  /**
   * Finds single stop in an organization by id
   * 
   * @param organizationId organization id
   * @param stopId stop id
   * @return stop or null if not found
   */
  public Stop findStop(OrganizationId organizationId, PublicTransportStopId stopId);

  /**
   * Lists stopTime in an organization
   * 
   * @param organizationId organization id
   * @param stopId return only stop times related to stopId (optional)
   * @param departureTime Filter stop times that depart in or after specified time. Value is defined in seconds since midnight (optional)
   * @return organization stopTimes
   */
  public List<StopTime> listStopTimes(OrganizationId organizationId, PublicTransportStopId stopId, Integer departureTime);
  
  /**
   * Finds single stopTime in an organization by id
   * 
   * @param organizationId organization id
   * @param stopTimeId stopTime id
   * @return stopTime or null if not found
   */
  public StopTime findStopTime(OrganizationId organizationId, PublicTransportStopTimeId stopTimeId);

  /**
   * Lists trip in an organization
   * 
   * @param organizationId organization id
   * @return organization trips
   */
  public List<Trip> listTrips(OrganizationId organizationId);
  
  /**
   * Finds single trip in an organization by id
   * 
   * @param organizationId organization id
   * @param tripId trip id
   * @return trip or null if not found
   */
  public Trip findTrip(OrganizationId organizationId, PublicTransportTripId tripId);  
}