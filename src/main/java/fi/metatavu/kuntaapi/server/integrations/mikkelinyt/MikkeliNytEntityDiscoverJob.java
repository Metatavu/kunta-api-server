package fi.metatavu.kuntaapi.server.integrations.mikkelinyt;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.BinaryHttpClient;
import fi.metatavu.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.mikkelinyt.resources.MikkeliNytEventResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.mikkelinyt.tasks.OrganizationEventsTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.mikkelinyt.model.Event;
import fi.otavanopisto.mikkelinyt.model.EventsResponse;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class MikkeliNytEntityDiscoverJob extends EntityDiscoverJob<OrganizationEntityUpdateTask> {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private GenericHttpClient httpClient;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;
  
  @Inject
  private MikkeliNytImageLoader mikkeliNytImageLoader;
  
  @Inject
  private MikkeliNytEventResourceContainer mikkeliNytEventResourceContainer;
  
  @Inject
  private MikkeliNytAttachmentDataResourceContainer mikkeliNytAttachmentDataResourceContainer;
  
  @Inject
  private MikkeliNytAttachmentResourceContainer mikkeliNytAttachmentResourceContainer;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private OrganizationEventsTaskQueue organizationEventsTaskQueue;

  @Inject
  private ScaledImageStore scaledImageStore;

  @Override
  public String getName() {
    return "mikkeli-nyt-events";
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationEventsTaskQueue.next();
      if (task != null) {
        execute(task);
      } else if (organizationEventsTaskQueue.isEmptyAndLocalNodeResponsible()) {
        organizationEventsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(MikkeliNytConsts.ORGANIZATION_SETTING_BASEURL));
      }
    }
  }
  
  @Override
  public void execute(OrganizationEntityUpdateTask task) {
    updateEvents(task.getOrganizationId());
  }
  
  private void updateEvents(OrganizationId organizationId) {
    Response<EventsResponse> response = listEvents(organizationId);
    if (response.isOk()) {
      List<Event> events = response.getResponseEntity().getData();
      
      List<EventId> existingEventIds = identifierRelationController.listEventIdsBySourceAndParentId(MikkeliNytConsts.IDENTIFIER_NAME, organizationId);
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        Long orderIndex = (long) i;
        EventId updatedEventId = updateEvent(organizationId, event, orderIndex);
        existingEventIds.remove(updatedEventId);
      }
      
      for (EventId existingEventId : existingEventIds) {
        deleteEvent(organizationId, existingEventId);
      }
      
    }
  }

  private EventId updateEvent(OrganizationId organizationId, Event mikkeliNytEvent, Long orderIndex) {
    EventId mikkeliNytEventId = new EventId(organizationId, MikkeliNytConsts.IDENTIFIER_NAME, mikkeliNytEvent.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, mikkeliNytEventId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    EventId kuntaApiId = new EventId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Event event = translate(kuntaApiId, mikkeliNytEvent);
    
    if (StringUtils.isNotBlank(mikkeliNytEvent.getImage())) {
      updateAttachment(organizationId, identifier, mikkeliNytEvent.getImage());
    }
    
    modificationHashCache.put(kuntaApiId.getId(), createPojoHash(event));
    mikkeliNytEventResourceContainer.put(kuntaApiId, event);
    
    return kuntaApiId;
  }

  private void updateAttachment(OrganizationId organizationId, Identifier eventIdentifier, String imageUrl) {
    AttachmentId mikkeliNytAttachmentId = getImageAttachmentId(organizationId, imageUrl);
    Long orderIndex = 0l;
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, mikkeliNytAttachmentId);
    identifierRelationController.addChild(eventIdentifier, identifier);
    AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Attachment attachment = translate(kuntaApiAttachmentId, imageUrl);

    mikkeliNytAttachmentResourceContainer.put(kuntaApiAttachmentId, attachment);
    
    AttachmentData imageData = mikkeliNytImageLoader.getImageData(mikkeliNytAttachmentId);
    if (imageData != null) {
      String dataHash = DigestUtils.md5Hex(imageData.getData());
      if (!dataHash.equals(modificationHashCache.get(identifier.getKuntaApiId()))) {
        modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
        mikkeliNytAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
        scaledImageStore.purgeStoredImages(kuntaApiAttachmentId);
      }
    }
  }
   
  private void deleteEvent(OrganizationId organizationId, EventId eventId) {
    Identifier eventIdentifier = identifierController.findIdentifierById(eventId);
    if (eventIdentifier != null) {
      EventId kuntaApiEventId = new EventId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, eventIdentifier.getKuntaApiId());
      modificationHashCache.clear(eventIdentifier.getKuntaApiId());
      mikkeliNytEventResourceContainer.clear(kuntaApiEventId);
      identifierController.deleteIdentifier(eventIdentifier);
    }
  }

  private Response<EventsResponse> listEvents(OrganizationId organizationId) {
    String location = organizationSettingController.getSettingValue(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_LOCATION);
    String baseUrl = organizationSettingController.getSettingValue(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_BASEURL);
    String apiKey = getApiKey(organizationId);
    
    URI uri;
    try {
      URIBuilder uriBuilder = new URIBuilder(String.format("%s%s", baseUrl, "/json.php?showall=1"));

      uriBuilder.addParameter("apiKey", apiKey);
      if (StringUtils.isNotBlank(location)) {
        uriBuilder.addParameter("location", location);
      } else {
        logger.warning("location not specified. Returning unfiltered event list");
      }
     
      uri = uriBuilder.build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, "Invalid uri", e);
      return new Response<>(500, "Internal Server Error", null);
    }
    
    return httpClient.doGETRequest(uri, new GenericHttpClient.ResultType<fi.otavanopisto.mikkelinyt.model.EventsResponse>() {});
  }
  
  private String getApiKey(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_APIKEY);
  }
  
  private fi.metatavu.kuntaapi.server.rest.model.Event translate(EventId kuntaApiId, fi.otavanopisto.mikkelinyt.model.Event nytEvent) {
    if (nytEvent == null) {
      return null;
    }
    
    fi.metatavu.kuntaapi.server.rest.model.Event result = new fi.metatavu.kuntaapi.server.rest.model.Event();
    
    result.setAddress(stripHtml(nytEvent.getAddress()));
    result.setCity(stripHtml(nytEvent.getCity()));
    result.setDescription(nytEvent.getDescription());
    result.setEnd(parseOffsetDateTime(nytEvent.getEnd()));
    result.setId(kuntaApiId.getId());
    result.setName(stripHtml(nytEvent.getName()));
    result.setOriginalUrl(nytEvent.getUrl());
    result.setPlace(stripHtml(nytEvent.getPlace()));
    result.setZip(stripHtml(nytEvent.getZip()));
    result.setStart(parseOffsetDateTime(nytEvent.getStart()));
    
    return result;
  }
  
  private Attachment translate(AttachmentId attachmentId, String imageUrl) {
    DownloadMeta downloadMeta = binaryHttpClient.getDownloadMeta(imageUrl);
    
    Attachment attachment = new Attachment();
    attachment.setId(attachmentId.getId());
    
    if (downloadMeta != null) {
      attachment.setContentType(downloadMeta.getContentType());
      attachment.setSize(downloadMeta.getSize() != null ? downloadMeta.getSize().longValue() : null);
    }
    
    return attachment;
  }
  
  private String stripHtml(String html) {
    return StringUtils.trim(StringUtils.normalizeSpace(StringEscapeUtils.unescapeHtml4(html.replaceAll("\\<.*?>"," "))));
  }
  
  private OffsetDateTime parseOffsetDateTime(String text) {
    LocalDateTime localDateTime = parseLocalDateTime(text);
    return localDateTime.atZone(ZoneId.of(MikkeliNytConsts.SERVER_TIMEZONE_ID)).toOffsetDateTime();
  }
  
  private AttachmentId getImageAttachmentId(OrganizationId organizationId, String url) {
    String imageId = StringUtils.substringAfterLast(url, "/");
    return new AttachmentId(organizationId, MikkeliNytConsts.IDENTIFIER_NAME, imageId);
  }
  
  private LocalDateTime parseLocalDateTime(String text) {
    
    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(' ')
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .toFormatter();
    
    return LocalDateTime.parse(text, formatter);
  }

}