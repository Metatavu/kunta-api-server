package fi.metatavu.kuntaapi.server.integrations.linkedevents;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Event;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.EventProvider;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsEventResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.AbstractAttachmentImageProvider;
import fi.metatavu.kuntaapi.server.resources.StoredBinaryData;

/**
 * Event provider for Linked events
 * 
 * @author Antti Leppä
 */

@ApplicationScoped
public class LinkedEventsEventProvider extends AbstractAttachmentImageProvider implements EventProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private LinkedEventsEventResourceContainer linkedEventsEventResourceContainer;
  
  @Inject
  private LinkedEventsAttachmentDataResourceContainer linkedEventsAttachmentDataResourceContainer;
  
  @Inject
  private LinkedEventsAttachmentResourceContainer linkedEventsAttachmentResourceContainer;
  
  @Inject
  private LinkedEventsImageLoader linkedEventsImageLoader;
  
  @Override
  public List<Event> listOrganizationEvents(OrganizationId organizationId, OffsetDateTime startBefore,
      OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter) {
    
    List<EventId> eventIds = identifierRelationController.listEventIdsBySourceAndParentId(LinkedEventsConsts.IDENTIFIER_NAME, organizationId);
    List<Event> result = new ArrayList<>(eventIds.size());
    
    for (EventId eventId : eventIds) {
      Event event = linkedEventsEventResourceContainer.get(eventId);
      if (event != null && isWithinTimeRanges(event, startBefore, startAfter, endBefore, endAfter)) {
        result.add(event); 
      }
    }
    
    return result;
  }

  @Override
  public Event findOrganizationEvent(OrganizationId organizationId, EventId eventId) {
    if (!identifierRelationController.isChildOf(organizationId, eventId)) {
      return null;
    }
    
    return linkedEventsEventResourceContainer.get(eventId);
  }

  @Override
  public List<Attachment> listEventImages(OrganizationId organizationId, EventId eventId) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(LinkedEventsConsts.IDENTIFIER_NAME, eventId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = linkedEventsAttachmentResourceContainer.get(attachmentId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }
  
  @Override
  public Attachment findEventImage(OrganizationId organizationId, EventId eventId, AttachmentId attachmentId) {
    if (!identifierRelationController.isChildOf(eventId, attachmentId)) {
      return null;
    }
    
    return linkedEventsAttachmentResourceContainer.get(attachmentId);
  }

  @Override
  public AttachmentData getEventImageData(OrganizationId organizationId, EventId eventId, AttachmentId kuntaApiAttachmentId, Integer size) {
    if (!identifierRelationController.isChildOf(eventId, kuntaApiAttachmentId)) {
      return null;
    }
    
    return getImageData(kuntaApiAttachmentId, size);
  }
  
  @Override
  protected AttachmentData getAttachmentData(AttachmentId kuntaApiAttachmentId) {
    AttachmentData storedAttachmentData = getStoredAttachmentData(kuntaApiAttachmentId);
    if (storedAttachmentData != null) {
      return storedAttachmentData;
    }
    
    return downloadImageData(kuntaApiAttachmentId);
  }
  
  private AttachmentData getStoredAttachmentData(AttachmentId attachmentId) {
    StoredBinaryData storedBinaryData = linkedEventsAttachmentDataResourceContainer.get(attachmentId);
    if (storedBinaryData != null) {
      try {
        return new AttachmentData(storedBinaryData.getContentType(), IOUtils.toByteArray(storedBinaryData.getDataStream()));
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to read stream data", e);
      } 
    }
     
    return null;
  }

  @SuppressWarnings ("squid:S1126")
  private boolean isWithinTimeRanges(Event event, OffsetDateTime startBefore,
      OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter) {
    
    OffsetDateTime eventStart = event.getStart();
    OffsetDateTime eventEnd = event.getEnd();
    
    if (eventStart == null) {
      logger.severe(() -> String.format("Event %s does not have start time", event.getId()));
      return false;
    }
    
    if (!isWithinTimeRange(startBefore, startAfter, eventStart)) {
      return false;
    }
    
    if (!isWithinTimeRange(endBefore, endAfter, eventEnd != null ? eventEnd : eventStart)) {
      return false;
    }
    
    return true;
  }

  @SuppressWarnings ("squid:S1126")
  private boolean isWithinTimeRange(OffsetDateTime before, OffsetDateTime after, OffsetDateTime time) {
    if (before != null && time.isAfter(before)) {
      return false;
    }

    if (after != null && time.isBefore(after)) {
      return false;
    }
    
    return true;
  }

  private AttachmentData downloadImageData(AttachmentId imageId) {
    AttachmentId linkedEventsId = idController.translateAttachmentId(imageId, LinkedEventsConsts.IDENTIFIER_NAME);
    if (linkedEventsId == null) {
      logger.severe(() -> String.format("Failed to translate %s into Linked events id", imageId.toString()));
      return null;
    }
    
    return linkedEventsImageLoader.getImageData(linkedEventsId);
  } 

  
}
