package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.Agency;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityUpdater;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportAgencyId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportAgencyResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyEntityTask;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class GtfsAgencyEntityUpdater extends EntityUpdater<GtfsAgencyEntityTask> {
  
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
  private GtfsPublicTransportAgencyResourceContainer gtfsPublicTransportAgencyCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private GtfsIdFactory gtfsIdFactory;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private GtfsAgencyTaskQueue gtfsAgencyTaskQueue;

  @Override
  public String getName() {
    return "gtfs-public-transport-agencies";
  }
  
  @Override
  public void timeout() {
    GtfsAgencyEntityTask task = gtfsAgencyTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  @Override
  public void execute(GtfsAgencyEntityTask task) {
    updateGtfsAgency(task);
  }
  
  private void updateGtfsAgency(GtfsAgencyEntityTask task) {
    Agency gtfsAgency = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", task.getOrganizationId()));
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    PublicTransportAgencyId gtfsAgencyId = gtfsIdFactory.createAgencyId(kuntaApiOrganizationId, gtfsAgency.getId());

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, gtfsAgencyId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportAgencyId kuntaApiAgencyId = kuntaApiIdFactory.createFromIdentifier(PublicTransportAgencyId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Agency agency = gtfsTranslator.translateAgency(kuntaApiAgencyId, gtfsAgency);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(agency));
    gtfsPublicTransportAgencyCache.put(kuntaApiAgencyId, agency);
  }

}
