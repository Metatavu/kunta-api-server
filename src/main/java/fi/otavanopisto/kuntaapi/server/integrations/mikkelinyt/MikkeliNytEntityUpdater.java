package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import fi.otavanopisto.kuntaapi.server.cache.EventCache;
import fi.otavanopisto.kuntaapi.server.cache.EventImageCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.mikkelinyt.model.Event;
import fi.otavanopisto.mikkelinyt.model.EventsResponse;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class MikkeliNytEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;
  
  @Inject
  private GenericHttpClient httpClient;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;
  
  @Inject
  private EventCache eventCache;
  
  @Inject
  private EventImageCache eventImageCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "mikkeli-nyt-events";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      if (getApiKey(organizationId) == null)  {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateEvents(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateEvents(OrganizationId organizationId) {
    Response<EventsResponse> response = listEvents(organizationId);
    if (response.isOk()) {
      for (fi.otavanopisto.mikkelinyt.model.Event event : response.getResponseEntity().getData()) {
        updateEvent(organizationId, event);
      }
    } else {
      logger.severe(String.format("Request list organization %s failed on [%d] %s", organizationId.toString(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateEvent(OrganizationId organizationId, Event mikkeliNytEvent) {
    EventId mikkeliNytEventId = new EventId(organizationId, MikkeliNytConsts.IDENTIFIER_NAME, mikkeliNytEvent.getId());
    
    Identifier identifier = identifierController.findIdentifierById(mikkeliNytEventId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(mikkeliNytEventId);
    }
    
    EventId kuntaApiId = new EventId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.otavanopisto.kuntaapi.server.rest.model.Event event = translate(kuntaApiId, mikkeliNytEvent);
    
    if (StringUtils.isNotBlank(mikkeliNytEvent.getImage())) {
      updateAttachment(organizationId, kuntaApiId, mikkeliNytEvent.getImage());
    }
    
    modificationHashCache.put(kuntaApiId.getId(), createPojoHash(event));
    eventCache.put(kuntaApiId, event);
  }

  private void updateAttachment(OrganizationId organizationId, EventId eventId, String imageUrl) {
    AttachmentId mikkeliNytAttachmentId = getImageAttachmentId(organizationId, imageUrl);
    
    Identifier identifier = identifierController.findIdentifierById(mikkeliNytAttachmentId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(mikkeliNytAttachmentId);
    }
    
    AttachmentId kuntaApiId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Attachment attachment = translate(kuntaApiId, imageUrl);

    modificationHashCache.put(kuntaApiId.getId(), createPojoHash(attachment));
    eventImageCache.put(new IdPair<EventId, AttachmentId>(eventId, kuntaApiId), attachment);
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
  
  private fi.otavanopisto.kuntaapi.server.rest.model.Event translate(EventId kuntaApiId, fi.otavanopisto.mikkelinyt.model.Event nytEvent) {
    if (nytEvent == null) {
      return null;
    }
    
    fi.otavanopisto.kuntaapi.server.rest.model.Event result = new fi.otavanopisto.kuntaapi.server.rest.model.Event();
    
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
