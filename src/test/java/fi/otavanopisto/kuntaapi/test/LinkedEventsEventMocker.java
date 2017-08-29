package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.linkedevents.client.model.Event;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.jackson.LinkedEventsTimeModule;

@SuppressWarnings ({"squid:S1166", "squid:S1075"})
public class LinkedEventsEventMocker extends AbstractBaseMocker {
  
  private static Logger logger = Logger.getLogger(LinkedEventsEventMocker.class.getName());
  private static final String PATH_TEMPLATE = "%s%s/";
  private static final String RESOURCES_PATH = "/v1/event/";
  private static final String FAILED_TO_READ_MOCK_FILE = "Failed to read mock file";
  
  private LinkedEventsResourceMocker<String, Event> eventMocker = new LinkedEventsResourceMocker<>();

  public LinkedEventsEventMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    eventMocker.start();
  }
  
  @Override
  public void endMock() {
    eventMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks linkedEvents events
   * 
   * @param ids event ids
   * @return mocker
   */
  public LinkedEventsEventMocker mockEvents(String... ids) {
    try {
      for (String id : ids) {
        if (!eventMocker.isMocked(id)) {
          Event event = readEventFromJSONFile(String.format("linkedevents/events/%s.json", id));
          mockEvent(event);
        } else {
          eventMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks linkedEvents events
   * 
   * @param ids event ids
   * @return mocker
   */
  public LinkedEventsEventMocker unmockEvents(String... ids) {
    for (String id : ids) {
      eventMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public LinkedEventsEventMocker mockLists() {
    mockList("1", "20");
    mockList("1", "1");

    return this;
  }

  private void mockList(String page, String pageSize) {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("page_size", containing(pageSize));
    queryParams.put("page", containing(page));
    eventMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);
  }
  
  
  
  private void mockEvent(Event event) throws JsonProcessingException {
    String eventId = event.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, eventId);
    eventMocker.add(eventId, event, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as event object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Event readEventFromJSONFile(String file) {
    return readJSONFile(file, Event.class);
  }
  
  @Override
  protected <T> T readJSONFile(String file, Class <T> type){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new LinkedEventsTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return objectMapper.readValue(stream, type);
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    return null;
  }
  
}
