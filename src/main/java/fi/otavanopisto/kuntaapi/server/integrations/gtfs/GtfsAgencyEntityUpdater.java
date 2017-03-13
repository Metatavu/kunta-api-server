package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.onebusaway.gtfs.model.Agency;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportAgencyCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class GtfsAgencyEntityUpdater extends EntityUpdater {
  
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
  private GtfsPublicTransportAgencyCache gtfsPublicTransportAgencyCache;
  
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
      updateGtfsAgency(task);
    }
  }
  
  private void updateGtfsAgency(GtfsAgencyEntityTask task) {
    Agency gtfsAgency = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, "Could not translate organization %s into Kunta API id", task.getOrganizationId());
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
