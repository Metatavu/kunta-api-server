package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

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
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.AbstractAttachmentImageProvider;
import fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytAttachmentDataResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytAttachmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytEventResourceContainer;
import fi.otavanopisto.kuntaapi.server.resources.StoredBinaryData;

/**
 * Event provider for Mikkeli Nyt
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */

@ApplicationScoped
public class MikkeliNytEventProvider extends AbstractAttachmentImageProvider implements EventProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private MikkeliNytEventResourceContainer mikkeliNytEventResourceContainer;
  
  @Inject
  private MikkeliNytAttachmentDataResourceContainer mikkeliNytAttachmentDataResourceContainer;
  
  @Inject
  private MikkeliNytAttachmentResourceContainer mikkeliNytAttachmentResourceContainer;
  
  @Inject
  private MikkeliNytImageLoader mikkeliNytImageLoader;
//
//  @Inject
//  private ImageReader imageReader;
//
//  @Inject
//  private ImageWriter imageWriter;
//  
//  @Inject
//  private ImageScaler imageScaler;
  
  @Override
  public List<Event> listOrganizationEvents(OrganizationId organizationId, OffsetDateTime startBefore,
      OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter) {
    
    List<EventId> eventIds = identifierRelationController.listEventIdsBySourceAndParentId(MikkeliNytConsts.IDENTIFIER_NAME, organizationId);
    List<Event> result = new ArrayList<>(eventIds.size());
    
    for (EventId eventId : eventIds) {
      Event event = mikkeliNytEventResourceContainer.get(eventId);
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
    
    return mikkeliNytEventResourceContainer.get(eventId);
  }

  @Override
  public List<Attachment> listEventImages(OrganizationId organizationId, EventId eventId) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(MikkeliNytConsts.IDENTIFIER_NAME, eventId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = mikkeliNytAttachmentResourceContainer.get(attachmentId);
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
    
    return mikkeliNytAttachmentResourceContainer.get(attachmentId);
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
    StoredBinaryData storedBinaryData = mikkeliNytAttachmentDataResourceContainer.get(attachmentId);
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
    
    if (eventStart == null || eventEnd == null) {
      return false;
    }
    
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
  
  private AttachmentData downloadImageData(AttachmentId imageId) {
    AttachmentId mikkeliNytId = idController.translateAttachmentId(imageId, MikkeliNytConsts.IDENTIFIER_NAME);
    if (mikkeliNytId == null) {
      logger.severe(() -> String.format("Failed to translate %s into MikkeliNyt id", imageId.toString()));
      return null;
    }
    
    return mikkeliNytImageLoader.getImageData(mikkeliNytId);
  } 

  
}
