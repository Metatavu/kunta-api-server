package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Event;
import fi.otavanopisto.kuntaapi.server.cache.EventCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;
import fi.otavanopisto.kuntaapi.server.images.ImageWriter;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

/**
 * Event provider for Mikkeli Nyt
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */

@RequestScoped
public class MikkeliNytEventProvider implements EventProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;

  @Inject
  private EventCache eventCache;
  
  @Inject
  private MikkeliNytAttachmentCache mikkeliNytAttachmentCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageWriter imageWriter;
  
  @Inject
  private ImageScaler imageScaler;
  
  @Override
  public List<Event> listOrganizationEvents(OrganizationId organizationId, OffsetDateTime startBefore,
      OffsetDateTime startAfter, OffsetDateTime endBefore, OffsetDateTime endAfter) {
    
    List<EventId> eventIds = identifierController.listEventIdsParentId(organizationId);
    List<Event> result = new ArrayList<>(eventIds.size());
    
    for (EventId eventId : eventIds) {
      Event event = eventCache.get(eventId);
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
    
    return eventCache.get(eventId);
  }

  @Override
  public List<Attachment> listEventImages(OrganizationId organizationId, EventId eventId) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsByParentId(organizationId, eventId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = mikkeliNytAttachmentCache.get(attachmentId);
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
    
    return mikkeliNytAttachmentCache.get(attachmentId);
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
  
  private AttachmentData scaleEventImage(AttachmentData imageData, Integer size) {
    BufferedImage bufferedImage = imageReader.readBufferedImage(imageData.getData());
    if (bufferedImage != null) {
      BufferedImage scaledImage = imageScaler.scaleMaxSize(bufferedImage, size);
      byte[] scaledImageData = imageWriter.writeBufferedImageAsPng(scaledImage);
      if (scaledImageData != null) {
        return new AttachmentData("image/png", scaledImageData);
      }
    }
    
    return null;
  }

  private Response<AttachmentData> getImageData(String imageUrl) {
    URI uri;
    
    try {
      uri = new URIBuilder(imageUrl).build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, String.format("Invalid uri %s", imageUrl), e);
      return new Response<>(500, "Internal Server Error", null);
    }

    Response<BinaryResponse> response = binaryHttpClient.downloadBinary(uri);
    AttachmentData data = null;
    if (response.getResponseEntity() != null) {
      data = new AttachmentData(response.getResponseEntity().getType(), response.getResponseEntity().getData());
    }
    
    return new Response<>(response.getStatus(), response.getMessage(), data);
  }
  
  private AttachmentData getImageData(OrganizationId organizationId, AttachmentId imageId) {
    AttachmentId mikkeliNytId = idController.translateAttachmentId(imageId, MikkeliNytConsts.IDENTIFIER_NAME);
    if (mikkeliNytId == null) {
      logger.severe(String.format("Failed to translate %s into MikkeliNyt id", imageId.toString()));
      return null;
    }
    
    String imageBaseUrl = organizationSettingController.getSettingValue(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_IMAGEBASEURL);
    if (StringUtils.isNotBlank(imageBaseUrl)) {
      String imageUrl = String.format("%s%s", imageBaseUrl, mikkeliNytId.getId());
      Response<AttachmentData> imageDataResponse = getImageData(imageUrl);
      if (imageDataResponse.isOk()) {
        return imageDataResponse.getResponseEntity();
      } else {
        logger.severe(String.format("Request to find image (%s) data failed on [%d] %s", imageId.toString(), imageDataResponse.getStatus(), imageDataResponse.getMessage()));
      }
    }
    
    logger.severe(String.format("Image imageBaseUrl has not been configured properly for organization %s", organizationId));
    
    return null;
  } 

  
}
