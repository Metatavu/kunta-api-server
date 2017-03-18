package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.Stop;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportStopResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsStopEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private GtfsTranslator gtfsTranslator;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private GtfsPublicTransportStopResourceContainer gtfsPublicTransportStopCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsStopTaskQueue gtfsStopTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

  @Override
  public void timeout() {
    GtfsStopEntityTask task = gtfsStopTaskQueue.next();
    if (task != null) {
      updateGtfsStop(task);
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }
  
  private void updateGtfsStop(GtfsStopEntityTask task) {
    Stop gtfsStop = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportStopId gtfsStopId = gtfsIdFactory.createStopId(kuntaApiOrganizationId, gtfsStop.getId().getId());

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, gtfsStopId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportStopId kuntaApiStopId = kuntaApiIdFactory.createFromIdentifier(PublicTransportStopId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Stop kuntaApiStop = gtfsTranslator.translateStop(kuntaApiStopId, gtfsStop);
    
    if (kuntaApiStop != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiStop));
      gtfsPublicTransportStopCache.put(kuntaApiStopId, kuntaApiStop);
    } else {
      logger.severe(String.format("Failed to translate gtfs stop %s", identifier.getKuntaApiId()));
    }

  }
}
