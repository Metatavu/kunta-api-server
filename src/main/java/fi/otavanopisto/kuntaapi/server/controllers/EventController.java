package fi.otavanopisto.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Event;

@ApplicationScoped
public class EventController {
  
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
    return result;
  }

  public List<Event> listEvents(OffsetDateTime startBefore, OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter,
      Integer firstResult, Integer maxResults, EventProvider.EventOrder order,
      EventProvider.EventOrderDirection orderDirection, OrganizationId organizationId) {
    List<Event> result = new ArrayList<>();
    for (EventProvider eventProvider : getEventProviders()) {
      result.addAll(eventProvider.listOrganizationEvents(organizationId, startBefore, startAfter, endBefore, endAfter, order, orderDirection, firstResult, maxResults));
    }
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
}
