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

import org.onebusaway.gtfs.model.Route;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportRouteCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsRouteEntityUpdater extends EntityUpdater {

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
  private GtfsPublicTransportRouteCache gtfsPublicTransportRouteCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsRouteTaskQueue gtfsRouteTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-public-transport-routes";
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
      GtfsRouteEntityTask task = gtfsRouteTaskQueue.next();
      if (task != null) {
        updateGtfsRoute(task);
      }
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateGtfsRoute(GtfsRouteEntityTask task) {
    Route gtfsRoute = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
      return;
    }
    
    PublicTransportAgencyId gtfsAgencyId = gtfsIdFactory.createAgencyId(kuntaApiOrganizationId, gtfsRoute.getAgency().getId());
    PublicTransportAgencyId kuntaApiAgencyId = idController.translatePublicTransportAgencyId(gtfsAgencyId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiAgencyId == null) {
      gtfsRouteTaskQueue.enqueueTask(false, task);
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportRouteId gtfsRouteId = gtfsIdFactory.createRouteId(kuntaApiOrganizationId, gtfsRoute.getId().getId());

    Identifier identifier = identifierController.findIdentifierById(gtfsRouteId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsRouteId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportRouteId kuntaApiRouteId = gtfsIdFactory.createKuntaApiId(PublicTransportRouteId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Route kuntaApiRoute = gtfsTranslator.translateRoute(kuntaApiRouteId, gtfsRoute, kuntaApiAgencyId);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiRoute));
    gtfsPublicTransportRouteCache.put(kuntaApiRouteId, kuntaApiRoute);
  }
}
