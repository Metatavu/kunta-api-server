package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.updaters;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.linkedevents.client.ApiResponse;
import fi.metatavu.linkedevents.client.EventApi;
import fi.metatavu.linkedevents.client.model.Event;
import fi.metatavu.linkedevents.client.model.Place;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.client.LinkedEventsApi;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsEventResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.tasks.LinkedEventsEventIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class LinkedEvenstEventEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private LinkedEventsTranslator linkedEventsTranslator;
  
  @Inject
  private LinkedEventsIdFactory linkedEventsIdFactory;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private LinkedEventsApi linkedEventsApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private LinkedEventsEventResourceContainer linkedEventsEventResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private LinkedEventsEventIdTaskQueue linkedEventsEventIdTaskQueue;

  @Override
  public String getName() {
    return "linkedevents-events";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  private void executeNextTask() {
    IdTask<EventId> task = linkedEventsEventIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateLinkedEventsEvent(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteLinkedEventsEvent(task.getId());
      }
    }
  }
  
  private void updateLinkedEventsEvent(EventId eventId, Long orderIndex) {
    OrganizationId organizationId = eventId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization linkedEvents baseUrl not set, skipping update"); 
      return;
    }
    
    EventApi eventApi = linkedEventsApi.getEventApi(organizationId);
    ApiResponse<Event> response = eventApi.eventRetrieve(eventId.getId());
    if (response.isOk()) {
      updateLinkedEventsEvent(organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find event %s event %s failed on [%d] %s", organizationId.getId(), eventId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateLinkedEventsEvent(OrganizationId organizationId, Event linkedEventsEvent, Long orderIndex) {
    EventId linkedEventsEventId = linkedEventsIdFactory.createEventId(organizationId, linkedEventsEvent.getId());
        
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, linkedEventsEventId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    EventId kuntaApiEventId = kuntaApiIdFactory.createFromIdentifier(EventId.class, identifier);
    // TODO: Load place
    Place place = null;
    
    fi.metatavu.kuntaapi.server.rest.model.Event event = linkedEventsTranslator.translateEvent(kuntaApiEventId, linkedEventsEvent, place);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(event));
    linkedEventsEventResourceContainer.put(kuntaApiEventId, event);
  }

  private void deleteLinkedEventsEvent(EventId linkedEventsEventId) {
    Identifier eventIdentifier = identifierController.findIdentifierById(linkedEventsEventId);
    if (eventIdentifier != null) {
      EventId kuntaApiEventId = kuntaApiIdFactory.createFromIdentifier(EventId.class, eventIdentifier);
      modificationHashCache.clear(eventIdentifier.getKuntaApiId());
      linkedEventsEventResourceContainer.clear(kuntaApiEventId);
      identifierController.deleteIdentifier(eventIdentifier);
    }
    
  }
}
