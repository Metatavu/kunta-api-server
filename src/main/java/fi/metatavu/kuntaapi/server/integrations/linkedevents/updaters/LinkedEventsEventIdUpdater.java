package fi.metatavu.kuntaapi.server.integrations.linkedevents.updaters;

import java.util.List;
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
import fi.metatavu.linkedevents.client.model.InlineResponse200;
import fi.metatavu.linkedevents.client.model.MetaDefinition;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdUpdater;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.LinkedEventsIdFactory;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.client.LinkedEventsApi;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.tasks.LinkedEventsEventIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.tasks.OrganizationLinkedEventsEventTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class LinkedEventsEventIdUpdater extends IdUpdater {

  private static final int PER_PAGE = 10;
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private LinkedEventsIdFactory linkedEventsIdFactory;
  
  @Inject
  private LinkedEventsApi linkedEventsApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private LinkedEventsEventIdTaskQueue linkedEventsEventIdTaskQueue;

  @Inject
  private OrganizationLinkedEventsEventTaskQueue organizationLinkedEventsEventTaskQueue;
  
  @Override
  public String getName() {
    return "linkedevents-event-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationLinkedEventsEventTaskQueue.next();
    if (task != null) {
      checkRemovedLinkedEventsEvent(task.getOrganizationId(), task.getOffset());
      updateLinkedEventsEvents(task.getOrganizationId(), task.getOffset());
    } else if (organizationLinkedEventsEventTaskQueue.isEmptyAndLocalNodeResponsible()) {
      List<OrganizationId> kuntaApiOrganizationIds = organizationSettingController.listOrganizationIdsWithSetting(LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL);
      for (OrganizationId kuntaApiOrganizationId : kuntaApiOrganizationIds) {
        long eventCount = getEventCount(kuntaApiOrganizationId);
        int batchCount = (int) Math.ceil(((float) eventCount) / PER_PAGE);
        for (int i = 0; i < batchCount; i++) {
          organizationLinkedEventsEventTaskQueue.enqueueTask(kuntaApiOrganizationId, i * PER_PAGE);
        } 
      }
    }
  }
  
  private void updateLinkedEventsEvents(OrganizationId organizationId, int offset) {
    if (!organizationSettingController.hasSettingValue(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization linkedevents baseUrl not set, skipping update"); 
      return;
    }
    
    int page = offset / PER_PAGE;
    
    String linkedEventsOrganization = organizationSettingController.getSettingValue(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_ORGANIZATION);
    
    EventApi eventApi = linkedEventsApi.getEventApi(organizationId);
    ApiResponse<InlineResponse200> response = eventApi.eventList(null, null, null, null, null, null, null, null, null, null, null, null, null, linkedEventsOrganization, null, page + 1, PER_PAGE);
    if (response.isOk()) {
      InlineResponse200 responseMeta = response.getResponse();
      MetaDefinition meta = responseMeta.getMeta();
      List<Event> events = responseMeta.getData();      
      if ((meta != null) && (events != null)) {
        for (int i = 0; i < events.size(); i++) {
          Event event = events.get(i);
          EventId eventId = linkedEventsIdFactory.createEventId(organizationId, event.getId());
          Long orderIndex = (long) (offset + i);
          linkedEventsEventIdTaskQueue.enqueueTask(new IdTask<EventId>(false, Operation.UPDATE, eventId, orderIndex));
        }
      }
    } else {
      logger.warning(String.format("Listing organization %s events failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void checkRemovedLinkedEventsEvent(OrganizationId organizationId, int offset) {
    EventApi eventApi = linkedEventsApi.getEventApi(organizationId);
    
    List<EventId> eventIds = identifierController.listOrganizationEventIdsBySource(organizationId, LinkedEventsConsts.IDENTIFIER_NAME, offset, PER_PAGE);
    for (EventId eventId : eventIds) {
      EventId linkedEventsEventId = idController.translateEventId(eventId, LinkedEventsConsts.IDENTIFIER_NAME);
      if (linkedEventsEventId != null) {
        ApiResponse<Event> response = eventApi.eventRetrieve(linkedEventsEventId.getId());
        int status = response.getStatus();
        if (status == 410) {
          linkedEventsEventIdTaskQueue.enqueueTask(new IdTask<EventId>(false, Operation.REMOVE, eventId));
        }
      }
    }
  }
  
  private long getEventCount(OrganizationId organizationId) {
    String linkedEventsOrganization = organizationSettingController.getSettingValue(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_ORGANIZATION);
    
    EventApi eventApi = linkedEventsApi.getEventApi(organizationId);
    ApiResponse<InlineResponse200> response = eventApi.eventList(null, null, null, null, null, null, null, null, null, null, null, null, null, linkedEventsOrganization, null, 1, 1);
    if (response.isOk()) {
      InlineResponse200 responseMeta = response.getResponse();
      return responseMeta.getMeta().getCount();
    }
    
    logger.severe("Failed to resolve event count from linked events");
    
    return 0;
  }

}
