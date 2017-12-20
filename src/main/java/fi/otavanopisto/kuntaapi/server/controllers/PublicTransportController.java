package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.search.StopTimeSearcher;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportStopTimeSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PublicTransportController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private StopTimeSearcher stopTimeSearcher;
  
  @Inject
  private Instance<PublicTransportProvider> publicTransportProviders;
  
  public List<Route> listRoutes(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Route> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listRoutes(organizationId));
    }
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
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
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
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

  @SuppressWarnings ("squid:S00107")
  public SearchResult<StopTime> searchStopTimes(OrganizationId organizationId, PublicTransportTripId tripId, PublicTransportStopId stopId, Integer depratureTimeOnOrAfter, PublicTransportStopTimeSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<PublicTransportStopTimeId> searchResult = stopTimeSearcher.searchStopTimes(
      organizationId != null ? organizationId.getId() : null, 
      tripId != null ? tripId.getId() : null, 
      stopId != null ? stopId.getId() : null, 
      depratureTimeOnOrAfter, 
      sortBy,
      sortDir,
      firstResult, 
      maxResults);
    
    if (searchResult != null) {
      List<StopTime> result = new ArrayList<>(searchResult.getResult().size());
      
      for (PublicTransportStopTimeId stopTimeId : searchResult.getResult()) {
        StopTime stopTime = findStopTime(organizationId, stopTimeId);
        if (stopTime != null) {
          result.add(stopTime);
        }
      }
      
      return new SearchResult<>(result, searchResult.getTotalHits());
    } else {
      List<StopTime> result = new ArrayList<>();
      
      for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
        result.addAll(publicTransportProvider.listStopTimes(organizationId, stopId, depratureTimeOnOrAfter));
      }
      
      return new SearchResult<>(ListUtils.limit(sortStopTimes(result, sortBy, sortDir), firstResult, maxResults), result.size());
    }
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

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
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
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
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
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
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
  
  private List<StopTime> sortStopTimes(List<StopTime> stopTimes, PublicTransportStopTimeSortBy sortBy, SortDir sortDir) {
    if (sortBy == null) {
      return entityController.sortEntitiesInNaturalOrder(stopTimes, sortDir);
    }
    
    if (sortBy == PublicTransportStopTimeSortBy.DEPARTURE_TIME) {
      Collections.sort(stopTimes, new DepartureTimeComparator(sortDir));
    }
    
    return stopTimes;
  }
  
  private List<PublicTransportProvider> getPublicTransportProviders() {
    List<PublicTransportProvider> result = new ArrayList<>();
    
    Iterator<PublicTransportProvider> iterator = publicTransportProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private class DepartureTimeComparator implements Comparator<StopTime> {

    private SortDir sortDir;
    
    public DepartureTimeComparator(SortDir sortDir) {
      this.sortDir = sortDir;
    }

    @Override
    public int compare(StopTime o1, StopTime o2) {
      int result = compareDepartureTimes(o1.getDepartureTime(), o2.getDepartureTime());
      if (sortDir == SortDir.DESC) {
        return -result;
      } 
      
      return result;
    }
    
    private int compareDepartureTimes(Integer departureTime1, Integer departureTime2) {
      if (departureTime1 == departureTime2) {
        return 0;
      }
      
      if (departureTime1 == null) {
        return -1;
      }

      if (departureTime2 == null) {
        return 1;
      }
      
      return departureTime1.compareTo(departureTime2);
    }
    
    
  }

}
