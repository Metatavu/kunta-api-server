package fi.otavanopisto.kuntaapi.server.integrations.linkedevents;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ezvcard.util.IOUtils;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Event;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;
import fi.otavanopisto.kuntaapi.server.images.ImageWriter;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentDataResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsEventResourceContainer;
import fi.otavanopisto.kuntaapi.server.resources.StoredBinaryData;

/**
 * Event provider for Linked events
 * 
 * @author Antti Leppä
 */

@ApplicationScoped
public class LinkedEventsEventProvider implements EventProvider {
  
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

  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageWriter imageWriter;
  
  @Inject
  private ImageScaler imageScaler;
  
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
  public AttachmentData getEventImageData(OrganizationId organizationId, EventId eventId, AttachmentId attachmentId, Integer size) {
    if (!identifierRelationController.isChildOf(eventId, attachmentId)) {
      return null;
    }
    
    AttachmentData imageData = getImageData(organizationId, attachmentId);
    if (size != null) {
      return scaleEventImage(imageData, size);
    }
    
    return imageData;
  }

  private AttachmentData getImageData(OrganizationId organizationId, AttachmentId attachmentId) {
    AttachmentData storedAttachmentData = getStoredAttachmentData(attachmentId);
    if (storedAttachmentData != null) {
      return storedAttachmentData;
    }
    
    return downloadImageData(organizationId, attachmentId);
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

  private boolean isWithinTimeRanges(Event event, OffsetDateTime startBefore,
      OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter) {
    
    OffsetDateTime eventStart = event.getStart();
    OffsetDateTime eventEnd = event.getEnd();
    
    if (!isWithinTimeRange(startBefore, startAfter, eventStart)) {
      return false;
    }
    
    if (!isWithinTimeRange(endBefore, endAfter, eventEnd)) {
      return false;
    }
    
    return true;
  }

  private boolean isWithinTimeRange(OffsetDateTime before, OffsetDateTime after, OffsetDateTime time) {
    if (before != null && time.isAfter(before)) {
      return false;
    }


    if (after != null && time.isBefore(after)) {
      return false;
    }
    
    return true;
  }
  
  private AttachmentData scaleEventImage(AttachmentData imageData, Integer size) {
    if (imageData == null) {
      return null;
    }
    
    BufferedImage bufferedImage = imageReader.readBufferedImage(imageData.getData());
    if (bufferedImage != null) {
      String formatName = imageWriter.getFormatName(imageData.getType());
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, size, true);
      byte[] scaledImageData = imageWriter.writeBufferedImage(scaledImage, formatName);
      if (scaledImageData != null) {
        String contentType = imageWriter.getContentTypeForFormatName(formatName);
        return new AttachmentData(contentType, scaledImageData);
      }
    }
    
    return null;
  }
  
  private AttachmentData downloadImageData(OrganizationId organizationId, AttachmentId imageId) {
    AttachmentId linkedEventsId = idController.translateAttachmentId(imageId, LinkedEventsConsts.IDENTIFIER_NAME);
    if (linkedEventsId == null) {
      logger.severe(String.format("Failed to translate %s into Linked events id", imageId.toString()));
      return null;
    }
    
    return linkedEventsImageLoader.getImageData(linkedEventsId);
  } 

  
}
