package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.StopTime;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportStopTimeCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsStopTimeEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private IdController idController;

  @Inject
  private GtfsTranslator gtfsTranslator;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private GtfsPublicTransportStopTimeCache gtfsPublicTransportStopTimeCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsStopTimeTaskQueue gtfsStopTimeTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-public-transport-stoptimes";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    GtfsStopTimeEntityTask task = gtfsStopTimeTaskQueue.next();
    if (task != null) {
      updateGtfsStopTime(task);
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateGtfsStopTime(GtfsStopTimeEntityTask task) {
    StopTime gtfsStopTime = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
      return;
    }
    
    PublicTransportStopId gtfsStopId = gtfsIdFactory.createStopId(kuntaApiOrganizationId, gtfsStopTime.getStop().getId().getId());
    PublicTransportStopId kuntaApiStopId = idController.translatePublicTransportStopId(gtfsStopId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiStopId == null) {
      gtfsStopTimeTaskQueue.enqueueTask(false, task);
      return;
    }
    
    PublicTransportTripId gtfsTripId = gtfsIdFactory.createTripId(kuntaApiOrganizationId, gtfsStopTime.getTrip().getId().getId());
    PublicTransportTripId kuntaApiTripId = idController.translatePublicTransportTripId(gtfsTripId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiTripId == null) {
      gtfsStopTimeTaskQueue.enqueueTask(false, task);
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportStopTimeId gtfsStopTimeId = gtfsIdFactory.createStopTimeId(kuntaApiOrganizationId, String.valueOf(gtfsStopTime.getId()));

    Identifier identifier = identifierController.findIdentifierById(gtfsStopTimeId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsStopTimeId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportStopTimeId kuntaApiStopTimeId = gtfsIdFactory.createKuntaApiId(PublicTransportStopTimeId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.StopTime kuntaApiStopTime = gtfsTranslator.translateStopTime(kuntaApiStopTimeId, gtfsStopTime, kuntaApiStopId, kuntaApiTripId);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiStopTime));
    gtfsPublicTransportStopTimeCache.put(kuntaApiStopTimeId, kuntaApiStopTime);
  }
}