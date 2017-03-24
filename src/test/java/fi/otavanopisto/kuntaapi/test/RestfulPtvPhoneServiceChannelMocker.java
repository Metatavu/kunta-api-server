package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.restfulptv.client.model.PhoneServiceChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvPhoneServiceChannelMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PHONE_CHANNELS_PATH = String.format("%s/phoneServiceChannels", AbstractIntegrationTest.BASE_URL);
  
  private ResourceMocker<String, PhoneServiceChannel> electornicServiceChannelMocker = new ResourceMocker<>();

  public RestfulPtvPhoneServiceChannelMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    electornicServiceChannelMocker.start();
  }
  
  @Override
  public void endMock() {
    electornicServiceChannelMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvPhoneServiceChannelMocker mockPhoneServiceChannels(String... ids) {
    try {
      for (String id : ids) {
        if (!electornicServiceChannelMocker.isMocked(id)) {
          mockPhoneServiceChannel(readPhoneServiceChannelFromJSONFile(String.format("phonechannels/%s.json", id)));
        } else {
          electornicServiceChannelMocker.setStatus(id, MockedResourceStatus.OK);
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
  public RestfulPtvPhoneServiceChannelMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      electornicServiceChannelMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvPhoneServiceChannelMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    electornicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PHONE_CHANNELS_PATH));
    electornicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PHONE_CHANNELS_PATH), queryParams);

    return this;
  }
  
  private void mockPhoneServiceChannel(PhoneServiceChannel phoneServiceChannel) throws JsonProcessingException {
    String phoneServiceChannelId = phoneServiceChannel.getId();
    String path = String.format(PATH_TEMPLATE, PHONE_CHANNELS_PATH, phoneServiceChannelId);
    electornicServiceChannelMocker.add(phoneServiceChannelId, phoneServiceChannel, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as phone service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PhoneServiceChannel readPhoneServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, PhoneServiceChannel.class);
  }
  
}
