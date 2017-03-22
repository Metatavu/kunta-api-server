package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvServiceLocationServiceChannelMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String SERVICE_LOCATION_CHANNELS_PATH = String.format("%s/serviceLocationServiceChannels", AbstractIntegrationTest.BASE_URL);
  
  private ResourceMocker<String, ServiceLocationServiceChannel> serviceLocationServiceChannelMocker = new ResourceMocker<>();

  public RestfulPtvServiceLocationServiceChannelMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    serviceLocationServiceChannelMocker.start();
  }
  
  @Override
  public void endMock() {
    serviceLocationServiceChannelMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvServiceLocationServiceChannelMocker mockServiceLocationServiceChannels(String... ids) {
    try {
      for (String id : ids) {
        if (!serviceLocationServiceChannelMocker.isMocked(id)) {
          mockServiceLocationServiceChannel(readServiceLocationServiceChannelFromJSONFile(String.format("servicelocationchannels/%s.json", id)));
        } else {
          serviceLocationServiceChannelMocker.setStatus(id, MockedResourceStatus.OK);
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
  public RestfulPtvServiceLocationServiceChannelMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      serviceLocationServiceChannelMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvServiceLocationServiceChannelMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    serviceLocationServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(SERVICE_LOCATION_CHANNELS_PATH));
    serviceLocationServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(SERVICE_LOCATION_CHANNELS_PATH), queryParams);

    return this;
  }
  
  private void mockServiceLocationServiceChannel(ServiceLocationServiceChannel serviceLocationServiceChannel) throws JsonProcessingException {
    String serviceLocationServiceChannelId = serviceLocationServiceChannel.getId();
    String path = String.format(PATH_TEMPLATE, SERVICE_LOCATION_CHANNELS_PATH, serviceLocationServiceChannelId);
    serviceLocationServiceChannelMocker.add(serviceLocationServiceChannelId, serviceLocationServiceChannel, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as serviceLocation service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private ServiceLocationServiceChannel readServiceLocationServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, ServiceLocationServiceChannel.class);
  }
  
}
