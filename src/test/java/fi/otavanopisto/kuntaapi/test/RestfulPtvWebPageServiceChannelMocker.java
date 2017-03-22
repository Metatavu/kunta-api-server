package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.restfulptv.client.model.WebPageServiceChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvWebPageServiceChannelMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String WEBPAGE_CHANNELS_PATH = String.format("%s/webPageServiceChannels", AbstractIntegrationTest.BASE_URL);
  
  private ResourceMocker<String, WebPageServiceChannel> webPageServiceChannelMocker = new ResourceMocker<>();

  public RestfulPtvWebPageServiceChannelMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    webPageServiceChannelMocker.start();
  }
  
  @Override
  public void endMock() {
    webPageServiceChannelMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvWebPageServiceChannelMocker mockWebPageServiceChannels(String... ids) {
    try {
      for (String id : ids) {
        if (!webPageServiceChannelMocker.isMocked(id)) {
          mockWebPageServiceChannel(readWebPageServiceChannelFromJSONFile(String.format("webpagechannels/%s.json", id)));
        } else {
          webPageServiceChannelMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }

  /**
   * Unmocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvWebPageServiceChannelMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      webPageServiceChannelMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvWebPageServiceChannelMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    webPageServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(WEBPAGE_CHANNELS_PATH));
    webPageServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(WEBPAGE_CHANNELS_PATH), queryParams);

    return this;
  }
  
  private void mockWebPageServiceChannel(WebPageServiceChannel webPageServiceChannel) throws JsonProcessingException {
    String webPageServiceChannelId = webPageServiceChannel.getId();
    String path = String.format(PATH_TEMPLATE, WEBPAGE_CHANNELS_PATH, webPageServiceChannelId);
    webPageServiceChannelMocker.add(webPageServiceChannelId, webPageServiceChannel, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as webPage service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private WebPageServiceChannel readWebPageServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, WebPageServiceChannel.class);
  }
  
}
