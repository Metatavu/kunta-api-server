package fi.otavanopisto.kuntaapi.server.integrations.linkedevents;

import java.util.Map;

import fi.metatavu.linkedevents.client.model.Event;
import fi.metatavu.linkedevents.client.model.EventInfoUrl;
import fi.metatavu.linkedevents.client.model.EventName;
import fi.metatavu.linkedevents.client.model.Place;
import fi.metatavu.linkedevents.client.model.PlaceAddressLocality;
import fi.metatavu.linkedevents.client.model.PlaceName;
import fi.otavanopisto.kuntaapi.server.id.EventId;

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
    result.setEnd(linkedEventsEvent.getEndTime());
    result.setId(kuntaApiId.getId());
    result.setName(name);
    result.setOriginalUrl(url);
    result.setPlace(place);
    result.setZip(zip);
    result.setStart(linkedEventsEvent.getStartTime());
    
    return result;
  }
  
  
}
