package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;
import org.onebusaway.gtfs.model.StopTime;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.metatavu.kuntaapi.server.id.PublicTransportTripId;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableStopTime;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportStopTimeResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeEntityTask;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = GtfsStopTimeTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.HIGH_CONCURRENCY_POOL)
public class GtfsStopTimeEntityDiscoverJob extends AbstractJmsJob<GtfsStopTimeEntityTask> {

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
  private GtfsPublicTransportStopTimeResourceContainer gtfsPublicTransportStopTimeCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Inject
  private GtfsStopTimeTaskQueue gtfsStopTimeTaskQueue;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Override
  public void execute(GtfsStopTimeEntityTask task) {
    updateGtfsStopTime(task);
  }
  
  private void updateGtfsStopTime(GtfsStopTimeEntityTask task) {
    StopTime gtfsStopTime = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", task.getOrganizationId()));
      return;
    }
    
    PublicTransportStopId gtfsStopId = gtfsIdFactory.createStopId(kuntaApiOrganizationId, gtfsStopTime.getStop().getId().getId());
    PublicTransportStopId kuntaApiStopId = idController.translatePublicTransportStopId(gtfsStopId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiStopId == null) {
      gtfsStopTimeTaskQueue.enqueueTask(task);
      return;
    }
    
    PublicTransportTripId gtfsTripId = gtfsIdFactory.createTripId(kuntaApiOrganizationId, gtfsStopTime.getTrip().getId().getId());
    PublicTransportTripId kuntaApiTripId = idController.translatePublicTransportTripId(gtfsTripId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiTripId == null) {
      gtfsStopTimeTaskQueue.enqueueTask(task);
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    
    PublicTransportStopTimeId gtfsStopTimeId = gtfsIdFactory.createStopTimeId(kuntaApiOrganizationId, String.valueOf(gtfsStopTime.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, gtfsStopTimeId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportStopTimeId kuntaApiStopTimeId = kuntaApiIdFactory.createFromIdentifier(PublicTransportStopTimeId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.StopTime kuntaApiStopTime = gtfsTranslator.translateStopTime(kuntaApiStopTimeId, gtfsStopTime, kuntaApiStopId, kuntaApiTripId);

    if (kuntaApiStopTime != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiStopTime));
      gtfsPublicTransportStopTimeCache.put(kuntaApiStopTimeId, kuntaApiStopTime);
      indexStopTime(kuntaApiOrganizationId, kuntaApiStopTime, orderIndex);
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate gtfs stoptime %s", identifier.getKuntaApiId()));
    }
  }

  private void indexStopTime(OrganizationId kuntaApiOrganizationId, fi.metatavu.kuntaapi.server.rest.model.StopTime kuntaApiStopTime, Long orderIndex) {
    IndexableStopTime indexableStopTime = new IndexableStopTime();
    indexableStopTime.setArrivalTime(kuntaApiStopTime.getArrivalTime());
    indexableStopTime.setDepartureTime(kuntaApiStopTime.getDepartureTime());
    indexableStopTime.setId(kuntaApiStopTime.getId());
    indexableStopTime.setStopId(kuntaApiStopTime.getStopId());
    indexableStopTime.setTripId(kuntaApiStopTime.getTripId());
    indexableStopTime.setOrganizationId(kuntaApiOrganizationId.getId());
    indexableStopTime.setOrderIndex(orderIndex);
    indexRequest.fire(new IndexRequest(indexableStopTime));
  }
}
