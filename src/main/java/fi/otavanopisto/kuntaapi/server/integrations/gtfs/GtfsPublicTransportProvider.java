package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.metatavu.kuntaapi.server.rest.model.StopTime;
import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportAgencyCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportRouteCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportScheduleCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportStopCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportStopTimeCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportTripCache;

public class GtfsPublicTransportProvider implements PublicTransportProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private GtfsPublicTransportAgencyCache gtfsPublicTransportAgencyCache;
  
  @Inject
  private GtfsPublicTransportScheduleCache gtfsPublicTransportScheduleCache;
  
  @Inject
  private GtfsPublicTransportRouteCache gtfsPublicTransportRouteCache;
  
  @Inject
  private GtfsPublicTransportStopCache gtfsPublicTransportStopCache;
    
  @Inject
  private GtfsPublicTransportStopTimeCache gtfsPublicTransportStopTimeCache;
      
  @Inject
  private GtfsPublicTransportTripCache gtfsPublicTransportTripCache;
  
  @Override
  public List<Agency> listAgencies(OrganizationId organizationId) {
    List<PublicTransportAgencyId> agencyIds = identifierRelationController.listPublicTransportAgencyIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Agency> agencies = new ArrayList<>(agencyIds.size());
    
    for (PublicTransportAgencyId agencyId : agencyIds) {
      Agency agency = gtfsPublicTransportAgencyCache.get(agencyId);
      if (agency != null) {
        agencies.add(agency);
      }
    }
    
    return agencies;
  }
  
  @Override
  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId) {
    if (!identifierRelationController.isChildOf(organizationId, agencyId)) {
      return null;
    }
    
    return gtfsPublicTransportAgencyCache.get(agencyId);
  }

  @Override
  public List<Schedule> listSchedules(OrganizationId organizationId) {
    List<PublicTransportScheduleId> scheduleIds = identifierRelationController.listPublicTransportScheduleIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Schedule> schedules = new ArrayList<>(scheduleIds.size());
    
    for (PublicTransportScheduleId scheduleId : scheduleIds) {
      Schedule schedule = gtfsPublicTransportScheduleCache.get(scheduleId);
      if (schedule != null) {
        schedules.add(schedule);
      }
    }
    
    return schedules;
  }

  @Override
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId) {
    if (!identifierRelationController.isChildOf(organizationId, scheduleId)) {
      return null;
    }
    
    return gtfsPublicTransportScheduleCache.get(scheduleId);
  }

  @Override
  public List<Route> listRoutes(OrganizationId organizationId) {
    List<PublicTransportRouteId> routeIds = identifierRelationController.listPublicTransportRouteIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Route> routes = new ArrayList<>(routeIds.size());
    
    for (PublicTransportRouteId routeId : routeIds) {
      Route route = gtfsPublicTransportRouteCache.get(routeId);
      if (route != null) {
        routes.add(route);
      }
    }
    
    return routes;
  }

  @Override
  public Route findRoute(OrganizationId organizationId, PublicTransportRouteId routeId) {
    if (!identifierRelationController.isChildOf(organizationId, routeId)) {
      return null;
    }
    
    return gtfsPublicTransportRouteCache.get(routeId);
  }
  
  @Override
  public List<Stop> listStops(OrganizationId organizationId) {
    List<PublicTransportStopId> stopIds = identifierRelationController.listPublicTransportStopIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Stop> stops = new ArrayList<>(stopIds.size());
    
    for (PublicTransportStopId stopId : stopIds) {
      Stop stop = gtfsPublicTransportStopCache.get(stopId);
      if (stop != null) {
        stops.add(stop);
      }
    }
    
    return stops;
  }

  @Override
  public Stop findStop(OrganizationId organizationId, PublicTransportStopId stopId) {
    if (!identifierRelationController.isChildOf(organizationId, stopId)) {
      return null;
    }
    
    return gtfsPublicTransportStopCache.get(stopId);
  }
  
  @Override
  public List<StopTime> listStopTimes(OrganizationId organizationId) {
    List<PublicTransportStopTimeId> stopTimeIds = identifierRelationController.listPublicTransportStopTimeIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<StopTime> stopTimes = new ArrayList<>(stopTimeIds.size());
    
    for (PublicTransportStopTimeId stopTimeId : stopTimeIds) {
      StopTime stopTime = gtfsPublicTransportStopTimeCache.get(stopTimeId);
      if (stopTime != null) {
        stopTimes.add(stopTime);
      }
    }
    
    return stopTimes;
  }

  @Override
  public StopTime findStopTime(OrganizationId organizationId, PublicTransportStopTimeId stopTimeId) {
    if (!identifierRelationController.isChildOf(organizationId, stopTimeId)) {
      return null;
    }
    
    return gtfsPublicTransportStopTimeCache.get(stopTimeId);
  }
  
  @Override
  public List<Trip> listTrips(OrganizationId organizationId) {
    List<PublicTransportTripId> tripIds = identifierRelationController.listPublicTransportTripIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Trip> trips = new ArrayList<>(tripIds.size());
    
    for (PublicTransportTripId tripId : tripIds) {
      Trip trip = gtfsPublicTransportTripCache.get(tripId);
      if (trip != null) {
        trips.add(trip);
      }
    }
    
    return trips;
  }

  @Override
  public Trip findTrip(OrganizationId organizationId, PublicTransportTripId tripId) {
    if (!identifierRelationController.isChildOf(organizationId, tripId)) {
      return null;
    }
    
    return gtfsPublicTransportTripCache.get(tripId);
  }

}
