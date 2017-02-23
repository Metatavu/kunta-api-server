package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.metatavu.kuntaapi.server.rest.model.StopTime;
import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PublicTransportController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<PublicTransportProvider> publicTransportProviders;
  
  public List<Route> listRoutes(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Route> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listRoutes(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public Route findRoute(OrganizationId organizationId, PublicTransportRouteId routeId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Route route = publicTransportProvider.findRoute(organizationId, routeId);
      if (route != null) {
        return route;
      }
    }
    
    return null;
  }
  
  public List<Stop> listStops(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Stop> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listStops(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public Stop findStop(OrganizationId organizationId, PublicTransportStopId stopId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Stop stop = publicTransportProvider.findStop(organizationId, stopId);
      if (stop != null) {
        return stop;
      }
    }
    
    return null;
  }
  
  public List<StopTime> listStopTimes(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<StopTime> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listStopTimes(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public StopTime findStopTime(OrganizationId organizationId, PublicTransportStopTimeId stopTimeId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      StopTime stopTime = publicTransportProvider.findStopTime(organizationId, stopTimeId);
      if (stopTime != null) {
        return stopTime;
      }
    }
    
    return null;
  }
  
  public List<Trip> listTrips(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Trip> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listTrips(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public Trip findTrip(OrganizationId organizationId, PublicTransportTripId tripId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Trip trip = publicTransportProvider.findTrip(organizationId, tripId);
      if (trip != null) {
        return trip;
      }
    }
    
    return null;
  }
  
  public List<Agency> listAgencies(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Agency> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listAgencies(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }

  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Agency agency = publicTransportProvider.findAgency(organizationId, agencyId);
      if (agency != null) {
        return agency;
      }
    }
    
    return null;
  }
  
  public List<Schedule> listSchedules(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Schedule> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listSchedules(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Schedule schedule = publicTransportProvider.findSchedule(organizationId, scheduleId);
      if (schedule != null) {
        return schedule;
      }
    }
    
    return null;
  }
  
  private List<PublicTransportProvider> getPublicTransportProviders() {
    List<PublicTransportProvider> result = new ArrayList<>();
    
    Iterator<PublicTransportProvider> iterator = publicTransportProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
