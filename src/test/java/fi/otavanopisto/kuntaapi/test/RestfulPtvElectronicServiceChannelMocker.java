package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.restfulptv.client.model.ElectronicServiceChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvElectronicServiceChannelMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String ELECTRONIC_CHANNELS_PATH = String.format("%s/electronicServiceChannels", AbstractIntegrationTest.BASE_URL);
  
  private ResourceMocker<String, ElectronicServiceChannel> electronicServiceChannelMocker = new ResourceMocker<>();

  public RestfulPtvElectronicServiceChannelMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    electronicServiceChannelMocker.start();
  }
  
  @Override
  public void endMock() {
    electronicServiceChannelMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvElectronicServiceChannelMocker mockElectronicServiceChannels(String... ids) {
    try {
      for (String id : ids) {
        if (!electronicServiceChannelMocker.isMocked(id)) {
          mockElectronicServiceChannel(readElectronicServiceChannelFromJSONFile(String.format("electronicservicechannels/%s.json", id)));
        } else {
          electronicServiceChannelMocker.setStatus(id, MockedResourceStatus.OK);
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
  public RestfulPtvElectronicServiceChannelMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      electronicServiceChannelMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvElectronicServiceChannelMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    electronicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(ELECTRONIC_CHANNELS_PATH));
    electronicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(ELECTRONIC_CHANNELS_PATH), queryParams);

    return this;
  }
  
  private void mockElectronicServiceChannel(ElectronicServiceChannel electronicServiceChannel) throws JsonProcessingException {
    String electronicServiceChannelId = electronicServiceChannel.getId();
    String path = String.format(PATH_TEMPLATE, ELECTRONIC_CHANNELS_PATH, electronicServiceChannelId);
    electronicServiceChannelMocker.add(electronicServiceChannelId, electronicServiceChannel, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as electronic service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private ElectronicServiceChannel readElectronicServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, ElectronicServiceChannel.class);
  }
  
}
