package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.Stop;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityUpdater;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportStopResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsStopEntityTask;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsStopEntityUpdater extends EntityUpdater<GtfsStopEntityTask> {

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

  @Override
  public String getName() {
    return "gtfs-public-transport-stops";
  }

  @Override
  public void timeout() {
    GtfsStopEntityTask task = gtfsStopTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  @Override
  public void execute(GtfsStopEntityTask task) {
    updateGtfsStop(task);
  }
  
  private void updateGtfsStop(GtfsStopEntityTask task) {
    Stop gtfsStop = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", task.getOrganizationId()));
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
      logger.log(Level.SEVERE, () -> String.format("Failed to translate gtfs stop %s", identifier.getKuntaApiId()));
    }

  }
}
