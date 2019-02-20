package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.linkedevents.client.model.Event;
import fi.metatavu.linkedevents.client.model.Image;

@SuppressWarnings ({"squid:S1166", "squid:S1075"})
public class LinkedEventsEventMocker extends AbstractBaseMocker {
  
  private static final String PATH_TEMPLATE = "%s%s/";
  private static final String RESOURCES_PATH = "/v1/event/";
  private static final String IMAGE_TYPE = "image/jpeg";
  
  private LinkedEventsResourceMocker<String, Event> eventMocker = new LinkedEventsResourceMocker<>();
  private LinkedEventsResourceMocker<String, Image> imageMocker = new LinkedEventsResourceMocker<>();
  private List<MappingBuilder> binaryStubs = new ArrayList<>();

  public LinkedEventsEventMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    imageMocker.start();
    eventMocker.start();
    
    for (MappingBuilder binaryStub : binaryStubs) {
      stubFor(binaryStub);
    }
  }
  
  @Override
  public void endMock() {
    eventMocker.stop();
    imageMocker.stop();
    
    for (MappingBuilder binaryStub : binaryStubs) {
      removeStub(binaryStub);
    }
    
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
          Event event = readEventFromJSONFile(String.format("linkedevents/events/%s.json", id.replaceAll(":", "_")));
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
   * Mocks resource with alternative contents
   * 
   * @param id id
   * @param alternative alternative postfix
   * @return mocker
   */
  public LinkedEventsEventMocker mockAlternative(String id, String alternative) {
    Event event = readEventFromJSONFile(String.format("linkedevents/events/%s_%s.json", id.replaceAll(":", "_"), alternative));
    eventMocker.mockAlternative(id, event);
    return this;
  }

  /**
   * Mocks linkedEvents images
   * 
   * @param ids images ids
   * @return mocker
   */
  public LinkedEventsEventMocker mockImages(String... ids) {
    try {
      for (String id : ids) {
        if (!eventMocker.isMocked(id)) {
          Image image = readImageFromJSONFile(String.format("linkedevents/images/%s.json", id));
          mockImage(image);
        } else {
          eventMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException | URISyntaxException e) {
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
  
  /**
   * Mocks an event 
   * 
   * @param event event
   * @throws JsonProcessingException thrown when JSON reading fails
   */
  private void mockEvent(Event event) throws JsonProcessingException {
    String eventId = event.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, eventId);
    eventMocker.add(eventId, event, urlPathEqualTo(path));
  }

  /**
   * Mocks event image
   * 
   * @param image image
   * @throws JsonProcessingException thrown when JSON reading fails
   * @throws URISyntaxException thrown when URI parsing fails
   */
  private void mockImage(Image image) throws JsonProcessingException, URISyntaxException {
    String id = String.valueOf(image.getId());
    String binaryUrl = image.getUrl();
    String binaryPath = new URI(binaryUrl).getPath();
    String resourcePath = String.format("/v1/image/%s/", id);
    mockBinary(binaryPath, IMAGE_TYPE, image.getName());
    imageMocker.add(id, image, urlPathEqualTo(resourcePath));
  }
  
  /**
   * Mocks binary response for GET request on path
   * 
   * @param path path
   * @param type response content type 
   * @param binaryFile path of mocked file
   */
  private void mockBinary(String path, String type, String binaryFile) {
    try (InputStream binaryStream = getClass().getClassLoader().getResourceAsStream(binaryFile)) {
      mockBinary(path, type, IOUtils.toByteArray(binaryStream));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
  
  /**
   * Mocks binary request
   * 
   * @param path path 
   * @param type type
   * @param binary binary
   */
  private void mockBinary(String path, String type, byte[] binary) {
    binaryStubs.add(head(urlPathEqualTo(path))
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, type)
      .withHeader(CONTENT_SIZE, String.valueOf(binary.length))
    ));
    
    binaryStubs.add(get(urlPathEqualTo(path))
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, type)
      .withBody(binary)
    ));
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

  /**
   * Reads JSON file as image object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Image readImageFromJSONFile(String file) {
    return readJSONFile(file, Image.class);
  }
  
}
