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

import org.onebusaway.gtfs.model.ServiceCalendar;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportScheduleCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsScheduleEntityUpdater extends EntityUpdater {

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
  private GtfsPublicTransportScheduleCache gtfsPublicTransportScheduleCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsScheduleTaskQueue gtfsScheduleTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-public-transport-schedules";
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
    GtfsScheduleEntityTask task = gtfsScheduleTaskQueue.next();
    if (task != null) {
      updateGtfsSchedule(task);
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateGtfsSchedule(GtfsScheduleEntityTask task) {
    ServiceCalendar gtfsServiceCalendar = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    PublicTransportScheduleId gtfsScheduleId = gtfsIdFactory.createScheduleId(kuntaApiOrganizationId, gtfsServiceCalendar.getServiceId().getId());

    Identifier identifier = identifierController.findIdentifierById(gtfsScheduleId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, gtfsScheduleId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportScheduleId kuntaApiScheduleId = gtfsIdFactory.createKuntaApiId(PublicTransportScheduleId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Schedule shedule = gtfsTranslator.translateSchedule(kuntaApiScheduleId, gtfsServiceCalendar, task.getExceptions());
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(shedule));
    gtfsPublicTransportScheduleCache.put(kuntaApiScheduleId, shedule);
  }

}
