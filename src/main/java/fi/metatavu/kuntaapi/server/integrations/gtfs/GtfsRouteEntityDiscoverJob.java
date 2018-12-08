package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;
import org.onebusaway.gtfs.model.Route;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportAgencyId;
import fi.metatavu.kuntaapi.server.id.PublicTransportRouteId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportRouteResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteEntityTask;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = GtfsRouteTaskQueue.JMS_QUEUE)
  }
)
@Pool(JmsQueueProperties.HIGH_CONCURRENCY_POOL)
public class GtfsRouteEntityDiscoverJob extends AbstractJmsJob<GtfsRouteEntityTask> {

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
  private GtfsPublicTransportRouteResourceContainer gtfsPublicTransportRouteCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsRouteTaskQueue gtfsRouteTaskQueue;

  @Override
  public void execute(GtfsRouteEntityTask task) {
    updateGtfsRoute(task);
  }
  
  private void updateGtfsRoute(GtfsRouteEntityTask task) {
    Route gtfsRoute = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", task.getOrganizationId()));
      return;
    }
    
    PublicTransportAgencyId gtfsAgencyId = gtfsIdFactory.createAgencyId(kuntaApiOrganizationId, gtfsRoute.getAgency().getId());
    PublicTransportAgencyId kuntaApiAgencyId = idController.translatePublicTransportAgencyId(gtfsAgencyId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiAgencyId == null) {
      gtfsRouteTaskQueue.enqueueTask(task);
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportRouteId gtfsRouteId = gtfsIdFactory.createRouteId(kuntaApiOrganizationId, gtfsRoute.getId().getId());

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, gtfsRouteId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportRouteId kuntaApiRouteId = kuntaApiIdFactory.createFromIdentifier(PublicTransportRouteId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Route kuntaApiRoute = gtfsTranslator.translateRoute(kuntaApiRouteId, gtfsRoute, kuntaApiAgencyId);
    if (kuntaApiRoute != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiRoute));
      gtfsPublicTransportRouteCache.put(kuntaApiRouteId, kuntaApiRoute);
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate gtfs route %s", identifier.getKuntaApiId()));
    }
  }
}
