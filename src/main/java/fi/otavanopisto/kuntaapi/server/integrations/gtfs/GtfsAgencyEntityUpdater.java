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

import org.onebusaway.gtfs.model.Agency;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportAgencyCache;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsAgencyEntityUpdater extends EntityUpdater {

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
  private GtfsPublicTransportAgencyCache gtfsPublicTransportAgencyCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private EntityUpdateRequestQueue<GtfsAgencyEntityUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new EntityUpdateRequestQueue<>();
  }

  @Override
  public String getName() {
    return "gtfs-public-transport-agencies";
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
  public void onFragmentIdUpdateRequest(@Observes GtfsAgencyEntityUpdateRequest event) {
    if (!stopped) {
      queue.add(event);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        GtfsAgencyEntityUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateGtfsAgency(updateRequest);
        }
      }
      
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateGtfsAgency(GtfsAgencyEntityUpdateRequest updateRequest) {
    Agency agency = updateRequest.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(updateRequest.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", updateRequest.getOrganizationId());
      return;
    }
    
    Long orderIndex = updateRequest.getOrderIndex();
    PublicTransportAgencyId gtfsAgencyId = new PublicTransportAgencyId(kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, String.format("%s-%s", kuntaApiOrganizationId.getId(), agency.getId()));

    Identifier identifier = identifierController.findIdentifierById(gtfsAgencyId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsAgencyId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportAgencyId kuntaApiAgencyId = new PublicTransportAgencyId(kuntaApiOrganizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Agency agengy = gtfsTranslator.translateAgengy(kuntaApiAgencyId, agency);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(agengy));
    gtfsPublicTransportAgencyCache.put(kuntaApiAgencyId, agengy);
  }

}
