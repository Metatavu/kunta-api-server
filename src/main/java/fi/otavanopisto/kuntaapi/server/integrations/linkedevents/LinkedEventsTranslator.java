package fi.otavanopisto.kuntaapi.server.integrations.linkedevents;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.linkedevents.client.model.Event;
import fi.metatavu.linkedevents.client.model.EventInfoUrl;
import fi.metatavu.linkedevents.client.model.EventName;
import fi.metatavu.linkedevents.client.model.Place;
import fi.metatavu.linkedevents.client.model.PlaceAddressLocality;
import fi.metatavu.linkedevents.client.model.PlaceName;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;

public class LinkedEventsTranslator {

  public fi.metatavu.kuntaapi.server.rest.model.Event translateEvent(EventId kuntaApiId, Event linkedEventsEvent, Place linkedEventsPlace) {
    if (linkedEventsEvent == null) {
      return null;
    }
    
    fi.metatavu.kuntaapi.server.rest.model.Event result = new fi.metatavu.kuntaapi.server.rest.model.Event();
    
    String city = null;
    String address = null;
    PlaceName linkedEventsPlaceName = null;

    if (linkedEventsPlace != null) {
      address = linkedEventsPlace.getStreetAddress() == null ? null : linkedEventsPlace.getStreetAddress().getFi();
      linkedEventsPlaceName = linkedEventsPlace.getName();
      PlaceAddressLocality linkedEventsAddressLocality = linkedEventsPlace.getAddressLocality();
      city = linkedEventsAddressLocality != null ? linkedEventsAddressLocality.getFi() : null;
    }
    
    EventName linkedEventsEventName = linkedEventsEvent.getName();
    EventInfoUrl linkedEventsInfoUrl = linkedEventsEvent.getInfoUrl();
    @SuppressWarnings("unchecked")
    Map<String, String> linkedEventsDescription = (Map<String, String>) linkedEventsEvent.getDescription();
    
    String name = linkedEventsEventName != null ? linkedEventsEventName.getFi() : null;
    String description = linkedEventsDescription.get("fi");
    String url = linkedEventsInfoUrl != null ? linkedEventsInfoUrl.getFi() : null;
    String place = linkedEventsPlaceName != null ? linkedEventsPlaceName.getFi() : null;
    String zip = linkedEventsPlace != null ? linkedEventsPlace.getPostalCode() : null;

    result.setAddress(address);
    result.setCity(city);
    result.setDescription(description);
    result.setEnd(translateTime(linkedEventsEvent.getEndTime()));
    result.setId(kuntaApiId.getId());
    result.setName(name);
    result.setOriginalUrl(url);
    result.setPlace(place);
    result.setZip(zip);
    result.setStart(translateTime(linkedEventsEvent.getStartTime()));
    
    return result;
  }

  public Attachment translateAttachment(AttachmentId kuntaApiAttachmentId, DownloadMeta imageMeta) {
    Attachment attachment = new Attachment();
    attachment.setId(kuntaApiAttachmentId.getId());
    
    if (imageMeta != null) {
      attachment.setContentType(imageMeta.getContentType());
      if (imageMeta.getSize() != null) {
        attachment.setSize(Long.valueOf(imageMeta.getSize()));
      }
    }
    
    attachment.setType(LinkedEventsConsts.ATTACHMENT_TYPE_EVENT_IMAGE);
    
    return attachment;
  }

  private OffsetDateTime translateTime(TemporalAccessor time) {
    if (time == null) {
      return null;
    }
    
    if (time.isSupported(ChronoField.HOUR_OF_DAY)) {
      return OffsetDateTime.from(time);
    }
    
    LocalDate localDate = LocalDate.from(time);
    if (localDate != null) {
      return localDate.atTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));
    }
    
    return null;
  }
  
  
}
