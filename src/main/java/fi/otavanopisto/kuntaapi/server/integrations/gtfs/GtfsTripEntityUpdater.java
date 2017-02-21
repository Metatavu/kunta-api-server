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

import org.onebusaway.gtfs.model.Trip;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportTripCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsTripEntityUpdater extends EntityUpdater {

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
  private GtfsPublicTransportTripCache gtfsPublicTransportTripCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsTripTaskQueue gtfsTripTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-public-transport-trips";
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
    if (systemSettingController.isNotTestingOrTestRunning()) {
      GtfsTripEntityTask task = gtfsTripTaskQueue.next();
      if (task != null) {
        updateGtfsTrip(task);
      }
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateGtfsTrip(GtfsTripEntityTask task) {
    Trip gtfsTrip = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
      return;
    }
    
    PublicTransportRouteId gtfsRouteId = gtfsIdFactory.createRouteId(kuntaApiOrganizationId, gtfsTrip.getRoute().getId().getId());
    PublicTransportRouteId kuntaApiRouteId = idController.translatePublicTransportRouteId(gtfsRouteId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiRouteId == null) {
      gtfsTripTaskQueue.enqueueTask(false, task);
      return;
    }
    
        
    PublicTransportScheduleId gtfsScheduleId = gtfsIdFactory.createScheduleId(kuntaApiOrganizationId, gtfsTrip.getServiceId().getId());
    PublicTransportScheduleId kuntaApiScheduleId = idController.translatePublicTransportScheduleId(gtfsScheduleId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiScheduleId == null) {
      gtfsTripTaskQueue.enqueueTask(false, task);
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportTripId gtfsTripId = gtfsIdFactory.createTripId(kuntaApiOrganizationId, gtfsTrip.getId().getId());

    Identifier identifier = identifierController.findIdentifierById(gtfsTripId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsTripId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportTripId kuntaApiTripId = gtfsIdFactory.createKuntaApiId(PublicTransportTripId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Trip kuntaApiTrip = gtfsTranslator.translateTrip(kuntaApiTripId, gtfsTrip, kuntaApiRouteId, kuntaApiScheduleId);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiTrip));
    gtfsPublicTransportTripCache.put(kuntaApiTripId, kuntaApiTrip);
  }
}
