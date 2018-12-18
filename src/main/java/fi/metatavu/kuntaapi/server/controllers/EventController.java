package fi.metatavu.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.EventProvider;
import fi.metatavu.kuntaapi.server.integrations.EventProvider.EventOrder;
import fi.metatavu.kuntaapi.server.integrations.EventProvider.EventOrderDirection;
import fi.metatavu.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Event;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class EventController {

  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<EventProvider> eventProviders;

  public Event findEvent(OrganizationId organizationId, EventId eventId) {
    for (EventProvider eventProvider : getEventProviders()) {
      Event event = eventProvider.findOrganizationEvent(organizationId, eventId);
      if (event != null) {
        return event;
      }
    }
    
    return null;
  }

  public Attachment findEventImage(OrganizationId organizationId, EventId eventId, AttachmentId attachmentId) {
    for (EventProvider eventProvider : getEventProviders()) {
      Attachment attachment = eventProvider.findEventImage(organizationId, eventId, attachmentId);
      if (attachment != null) {
        return attachment;
      }
    }
    
    return null;
  }
  
  public AttachmentData getEventImageData(Integer size, OrganizationId organizationId, EventId eventId, AttachmentId attachmentId) {
    for (EventProvider eventProvider : getEventProviders()) {
      AttachmentData attachmentData = eventProvider.getEventImageData(organizationId, eventId, attachmentId, size);
      if (attachmentData != null) {
        return attachmentData;
      }
    }
    
    return null;
  }
  
  public List<Attachment> listEventImages(OrganizationId organizationId, EventId eventId) {
    List<Attachment> result = new ArrayList<>();
    
    for (EventProvider eventProvider : getEventProviders()) {
      result.addAll(eventProvider.listEventImages(organizationId, eventId));
    }
    
    return entityController.sortEntitiesInNaturalOrder(result);
  }

  @SuppressWarnings ("squid:S00107")
  public List<Event> listEvents(OffsetDateTime startBefore, OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter,
      Integer firstResult, Integer maxResults, EventProvider.EventOrder order,
      EventProvider.EventOrderDirection orderDirection, OrganizationId organizationId) {
    List<Event> result = new ArrayList<>();
    
    for (EventProvider eventProvider : getEventProviders()) {
      result.addAll(eventProvider.listOrganizationEvents(organizationId, startBefore, startAfter, endBefore, endAfter));
    }
    
    return ListUtils.limit(sortEvents(result, order, orderDirection), firstResult, maxResults);
  }
  
  private List<Event> sortEvents(List<Event> result, EventProvider.EventOrder order, EventProvider.EventOrderDirection orderDirection) {
    if (order == null) {
      return entityController.sortEntitiesInNaturalOrder(result);
    }
    
    Collections.sort(result, new EventComparator(order, orderDirection));
    return result;
  }
  
  private List<EventProvider> getEventProviders() {
    List<EventProvider> result = new ArrayList<>();
    
    Iterator<EventProvider> iterator = eventProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  } 
  
  private class EventComparator implements Comparator<Event> {
    
    private EventOrder order;
    private EventOrderDirection direction;
    
    public EventComparator(EventOrder order, EventOrderDirection direction) {
      this.order = order;
      this.direction = direction;
    }
    
    @Override
    public int compare(Event event1, Event event2) {
      int result;
      
      switch (order) {
        case END_DATE:
          result = compareEndDates(event1, event2);
        break;
        case START_DATE:
          result = compareStartDates(event1, event2);
        break;
        default:
          result = 0;
        break;
      }
      
      if (direction == EventOrderDirection.ASCENDING) {
        return -result;
      } 
      
      return result;
    }

    private int compareStartDates(Event event1, Event event2) {
      return compareDates(event1.getStart(), event2.getStart());
    }

    private int compareEndDates(Event event1, Event event2) {
      return compareDates(event1.getEnd(), event2.getEnd());
    }
    
    private int compareDates(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
      if (dateTime1 == null && dateTime2 == null) {
        return 0;
      }
      
      if (dateTime1 == null) {
        return 1;
      } else if (dateTime2 == null) {
        return -1;
      }
              
      return dateTime1.compareTo(dateTime2);
    }
    
  }
}
