package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.linkedevents.client.ApiResponse;
import fi.metatavu.linkedevents.client.EventApi;
import fi.metatavu.linkedevents.client.FilterApi;
import fi.metatavu.linkedevents.client.model.Event;
import fi.metatavu.linkedevents.client.model.IdRef;
import fi.metatavu.linkedevents.client.model.Image;
import fi.metatavu.linkedevents.client.model.Place;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsImageLoader;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.client.LinkedEventsApi;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentDataResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources.LinkedEventsAttachmentResourceContainer;
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
  private LinkedEventsAttachmentDataResourceContainer linkedEventsAttachmentDataResourceContainer;
  
  @Inject
  private LinkedEventsAttachmentResourceContainer linkedEventsAttachmentResourceContainer;
  
  @Inject
  private LinkedEventsImageLoader linkedEventsImageLoader;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private LinkedEventsEventIdTaskQueue linkedEventsEventIdTaskQueue;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;
  
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
  
  private void updateLinkedEventsEvent(OrganizationId kuntaApiOrganizationId, Event linkedEventsEvent, Long orderIndex) {
    EventId linkedEventsEventId = linkedEventsIdFactory.createEventId(kuntaApiOrganizationId, linkedEventsEvent.getId());
        
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, linkedEventsEventId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    Place place = null;
    
    EventId kuntaApiEventId = kuntaApiIdFactory.createFromIdentifier(EventId.class, identifier);
    if (linkedEventsEvent.getLocation() != null) {
      IdRef locationIdRef = linkedEventsEvent.getLocation();
      String placeId = extractIdRefId(locationIdRef);
      if (placeId != null) {
        FilterApi filterApi = linkedEventsApi.getFilterApi(kuntaApiOrganizationId);
        ApiResponse<Place> placeResponse = filterApi.placeRetrieve(placeId);
        if (!placeResponse.isOk()) {
          logger.log(Level.INFO, () -> String.format("Failed to load place (%s) from LinkedEvents event %s", linkedEventsEvent.getId(), placeId)); 
          return;
        } else {
          place = placeResponse.getResponse();
        }
      }
    }
    
    fi.metatavu.kuntaapi.server.rest.model.Event event = linkedEventsTranslator.translateEvent(kuntaApiEventId, linkedEventsEvent, place);
    
    updateImages(kuntaApiOrganizationId, identifier, linkedEventsEvent.getImages());
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(event));
    linkedEventsEventResourceContainer.put(kuntaApiEventId, event);
  }

  private void updateImages(OrganizationId organizationId, Identifier eventIdentifier, List<Image> images) {
    for (Image image : images) {
      updateImage(organizationId, eventIdentifier, image);
    }
  }

  private void updateImage(OrganizationId kuntaApiOrganizationId, Identifier eventIdentifier, Image linkedEventsImage) {
    AttachmentId linkedEventsAttachmentId = linkedEventsIdFactory.createAttachmentId(kuntaApiOrganizationId, String.valueOf(linkedEventsImage.getId()));
    Long orderIndex = 0l;
    
    Identifier imageIdentifier = identifierController.acquireIdentifier(orderIndex, linkedEventsAttachmentId);
    identifierRelationController.addChild(eventIdentifier, imageIdentifier);
    AttachmentId kuntaApiAttachmentId = kuntaApiIdFactory.createFromIdentifier(AttachmentId.class, imageIdentifier);
    DownloadMeta downloadMeta = binaryHttpClient.getDownloadMeta(linkedEventsImage.getUrl());
    if (downloadMeta == null) {
      logger.log(Level.INFO, () -> String.format("Failed to download meta for LinkedEvents image %s (%s)", linkedEventsImage.getId(), linkedEventsImage.getUrl())); 
      return;
    }
    
    Attachment attachment = linkedEventsTranslator.translateAttachment(kuntaApiAttachmentId, downloadMeta);

    linkedEventsAttachmentResourceContainer.put(kuntaApiAttachmentId, attachment);
    
    AttachmentData imageData = linkedEventsImageLoader.getImageData(linkedEventsAttachmentId);
    if (imageData != null) {
      String dataHash = DigestUtils.md5Hex(imageData.getData());
      modificationHashCache.put(imageIdentifier.getKuntaApiId(), dataHash);
      linkedEventsAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
    }
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
  
  private String extractIdRefId(IdRef locationIdRef) {
    if (locationIdRef == null || StringUtils.isBlank(locationIdRef.getId())) {
      return null;
    }
    
    String[] parts = StringUtils.split(StringUtils.removeEnd(locationIdRef.getId(), "/"), '/');
    return parts[parts.length - 1];
  }
}
