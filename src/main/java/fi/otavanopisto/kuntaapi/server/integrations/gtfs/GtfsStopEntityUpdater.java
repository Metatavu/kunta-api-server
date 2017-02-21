package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.Stop;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportStopCache;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsStopEntityUpdater extends EntityUpdater {

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
  private GtfsPublicTransportStopCache gtfsPublicTransportStopCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private EntityUpdateRequestQueue<GtfsStopEntityUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new EntityUpdateRequestQueue<>();
  }

  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onStopIdUpdateRequest(@Observes GtfsStopEntityUpdateRequest event) {
    if (!stopped) {
      queue.add(event);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        GtfsStopEntityUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateGtfsStop(updateRequest);
        }
      }
      
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateGtfsStop(GtfsStopEntityUpdateRequest updateRequest) {
    Stop gtfsStop = updateRequest.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(updateRequest.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", updateRequest.getOrganizationId());
      return;
    }
    
    Long orderIndex = updateRequest.getOrderIndex();
    
    PublicTransportStopId gtfsStopId = gtfsIdFactory.createStopId(kuntaApiOrganizationId, gtfsStop.getId().getId());

    Identifier identifier = identifierController.findIdentifierById(gtfsStopId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsStopId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportStopId kuntaApiStopId = gtfsIdFactory.createKuntaApiId(PublicTransportStopId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Stop kuntaApiStop = gtfsTranslator.translateStop(kuntaApiStopId, gtfsStop);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiStop));
    gtfsPublicTransportStopCache.put(kuntaApiStopId, kuntaApiStop);
  }
}
