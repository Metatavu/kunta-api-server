package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvPrintableFormServiceChannelMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PRINTABLE_FORM_CHANNELS_PATH = String.format("%s/printableFormServiceChannels", AbstractIntegrationTest.BASE_URL);
  
  private ResourceMocker<String, PrintableFormServiceChannel> electornicServiceChannelMocker = new ResourceMocker<>();

  public RestfulPtvPrintableFormServiceChannelMocker() {
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
  public RestfulPtvPrintableFormServiceChannelMocker mockPrintableFormServiceChannels(String... ids) {
    try {
      for (String id : ids) {
        if (!electornicServiceChannelMocker.isMocked(id)) {
          mockPrintableFormServiceChannel(readPrintableFormServiceChannelFromJSONFile(String.format("printableformchannels/%s.json", id)));
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
  public RestfulPtvPrintableFormServiceChannelMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      electornicServiceChannelMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvPrintableFormServiceChannelMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    electornicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PRINTABLE_FORM_CHANNELS_PATH));
    electornicServiceChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PRINTABLE_FORM_CHANNELS_PATH), queryParams);

    return this;
  }
  
  private void mockPrintableFormServiceChannel(PrintableFormServiceChannel printableFormServiceChannel) throws JsonProcessingException {
    String printableFormServiceChannelId = printableFormServiceChannel.getId();
    String path = String.format(PATH_TEMPLATE, PRINTABLE_FORM_CHANNELS_PATH, printableFormServiceChannelId);
    electornicServiceChannelMocker.add(printableFormServiceChannelId, printableFormServiceChannel, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as printableForm service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PrintableFormServiceChannel readPrintableFormServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, PrintableFormServiceChannel.class);
  }
  
}
