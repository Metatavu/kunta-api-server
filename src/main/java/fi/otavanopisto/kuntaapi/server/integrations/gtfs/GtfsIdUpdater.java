package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.OrganizationGtfsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class GtfsIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationGtfsTaskQueue organizationGtfsTaskQueue;
  
  @Inject
  private GtfsAgencyTaskQueue agencyTaskQueue;
  
  @Inject
  private GtfsScheduleTaskQueue scheduleTaskQueue;
  
  @Inject
  private GtfsRouteTaskQueue routeTaskQueue;
  
  @Inject
  private GtfsStopTaskQueue stopTaskQueue;
  
  @Inject
  private GtfsStopTimeTaskQueue stopTimeTaskQueue;
  
  @Inject
  private GtfsTripTaskQueue tripTaskQueue;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationGtfsTaskQueue.next();
    if (task != null) {
      updateGtfsEntities(task.getOrganizationId());
    } else {
      organizationGtfsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH));
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }
  
  private void updateGtfsEntities(OrganizationId organizationId) {
    if(organizationSettingController.getSettingValue(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE) == null) {
      logger.log(Level.WARNING, () -> String.format("Tried to update organization: %s GTFS - data without gtfs timezone", organizationId.getId()));
      return;
    }
    
    GtfsReader reader = new GtfsReader();
    String gtfsFolderPath = organizationSettingController.getSettingValue(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH);
    if(gtfsFolderPath == null) {
      logger.log(Level.WARNING, () -> String.format("Tried to update organization: %s GTFS - data for without folder path", organizationId.getId()));
      return;
    }
    
    File organizationGtfsFolder = new File(gtfsFolderPath);
    if(!organizationGtfsFolder.exists() || !organizationGtfsFolder.isDirectory()) {
      logger.log(Level.WARNING, () -> String.format("gtfs folder with path %s for organization %s doesnt exist", gtfsFolderPath, organizationId.getId()));
      return;
    }
    
    try {
      reader.setInputLocation(organizationGtfsFolder);
      GtfsDaoImpl store = new GtfsDaoImpl();
      reader.setEntityStore(store);
      reader.run();
      
      handleAgencies(organizationId, store);
      handleStops(organizationId, store);
      handleSchedules(organizationId, store);
      handleRoutes(organizationId, store);
      handleTrips(organizationId, store);
      handleStopTimes(organizationId, store);
      
    } catch (IOException e) {
      if (logger.isLoggable(Level.WARNING)) {
        logger.log(Level.WARNING, String.format("Failed to update GTFS - data for organization: %s", organizationId.getId()), e);
      }
    }
  }
  
  private void handleAgencies(OrganizationId organizationId, GtfsDaoImpl store){
    Collection<Agency> agencies = store.getAllAgencies();
    List<Agency> agencyList = new ArrayList<>(agencies);
    for(int i = 0; i < agencyList.size(); i++) {
      Agency agency = agencyList.get(i);
      agencyTaskQueue.enqueueTask(false, new GtfsAgencyEntityTask(organizationId, agency, (long) i));
    }
  }

  private void handleStops(OrganizationId organizationId, GtfsDaoImpl store){
    Collection<Stop> stops = store.getAllStops();
    List<Stop> stopList = new ArrayList<>(stops);
    for(int i = 0; i < stopList.size(); i++) {
      Stop stop = stopList.get(i);
      stopTaskQueue.enqueueTask(false, new GtfsStopEntityTask(organizationId, stop, (long) i));
    }
  }
  
  private void handleStopTimes(OrganizationId organizationId, GtfsDaoImpl store){
    Collection<StopTime> stopTimes = store.getAllStopTimes();
    List<StopTime> stopTimeList = new ArrayList<>(stopTimes);
    for(int i = 0; i < stopTimeList.size(); i++) {
      StopTime stopTime = stopTimeList.get(i);
      stopTimeTaskQueue.enqueueTask(false, new GtfsStopTimeEntityTask(organizationId, stopTime, (long) i));
    }
  }
    
  private void handleTrips(OrganizationId organizationId, GtfsDaoImpl store){
    Collection<Trip> trips = store.getAllTrips();
    List<Trip> tripList = new ArrayList<>(trips);
    for(int i = 0; i < tripList.size(); i++) {
      Trip trip = tripList.get(i);
      tripTaskQueue.enqueueTask(false, new GtfsTripEntityTask(organizationId, trip, (long) i));
    }
  }
  
  private void handleSchedules(OrganizationId organizationId, GtfsDaoImpl store) {
    Collection<ServiceCalendar> serviceCalendars = store.getAllCalendars();
    List<ServiceCalendar> serviceCalendarList = new ArrayList<>(serviceCalendars);
    for (int i = 0; i < serviceCalendarList.size(); i++) {
      ServiceCalendar serviceCalendar = serviceCalendarList.get(i);
      List<ServiceCalendarDate> exceptions = getExectionsByServiceCalendar(store, serviceCalendar);
      scheduleTaskQueue.enqueueTask(false, new GtfsScheduleEntityTask(organizationId, serviceCalendar, exceptions, (long) i));
    }
  }
  
  private void handleRoutes(OrganizationId organizationId, GtfsDaoImpl store) {
    Collection<Route> routes = store.getAllRoutes();
    List<Route> routeList = new ArrayList<>(routes);
    for (int i = 0; i < routeList.size(); i++) {
      Route route = routeList.get(i);
      List<ServiceCalendar> serviceCalendars = getServiceCalendarsByRoute(store, route);
      routeTaskQueue.enqueueTask(false, new GtfsRouteEntityTask(organizationId, route, serviceCalendars, (long) i));
    }
  }
  
  private List<ServiceCalendar> getServiceCalendarsByRoute(GtfsDaoImpl store, Route route) {
    List<ServiceCalendar> results = new ArrayList<>();
    Collection<Trip> trips = store.getAllTrips();
    for (Trip trip : trips) {
      if (trip.getRoute().getId().equals(route.getId())) {
        ServiceCalendar serviceCalendar = getServiceCalendarByServiceId(store, trip.getServiceId());
        if(serviceCalendar != null) {
          results.add(serviceCalendar);
        }
      }
    }
    
    return results;
  }
  
  private ServiceCalendar getServiceCalendarByServiceId(GtfsDaoImpl store, AgencyAndId serviceId) {
    Collection<ServiceCalendar> serviceCalendars = store.getAllCalendars();
    for (ServiceCalendar serviceCalendar : serviceCalendars) {
      if(serviceCalendar.getServiceId().equals(serviceId)) {
        return serviceCalendar;
      }
    }
    return null;
  }
  
  private List<ServiceCalendarDate> getExectionsByServiceCalendar(GtfsDaoImpl store, ServiceCalendar serviceCalendar) {
    List<ServiceCalendarDate> results = new ArrayList<>();
    Collection<ServiceCalendarDate> serviceCalendarDates = store.getAllCalendarDates();
    for (ServiceCalendarDate serviceCalendarDate : serviceCalendarDates) {
      if(serviceCalendar.getServiceId().equals(serviceCalendarDate.getServiceId())) {
        results.add(serviceCalendarDate);
      }
    }
    
    return results;
  }
  
}
