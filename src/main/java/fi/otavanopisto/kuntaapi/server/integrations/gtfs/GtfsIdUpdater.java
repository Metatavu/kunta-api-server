package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.serialization.GtfsReader;

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsIdUpdater extends IdUpdater {


  private static final int WARMUP_TIME = 100 * 10;
  private static final int TIMER_INTERVAL = 1000;
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private Event<GtfsAgencyEntityUpdateRequest> agencyUpdateRequest;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  private boolean stopped;
  private List<OrganizationId> queue;
  
  @Resource
  private TimerService timerService;
  
  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "gtfs-public-transport-agency-ids";
  }
  
  @Override
  public void startTimer() {
    stopped = false;
    startTimer(WARMUP_TIME);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning() && !queue.isEmpty()) {
        updateGtfsEntities(queue.remove(0));
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateGtfsEntities(OrganizationId organizationId) {
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
      agencyUpdateRequest.fire(new GtfsAgencyEntityUpdateRequest(organizationId, agency, (long) i, false));
    }
  }
}
