package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Shortlink;

@SuppressWarnings ("squid:S1166")
public class ManagementShortlinkMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/shortlink";
  
  private ResourceMocker<Integer, Shortlink> shortlinkMocker = new ResourceMocker<>();

  public ManagementShortlinkMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    shortlinkMocker.start();
  }
  
  @Override
  public void endMock() {
    shortlinkMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management shortlinks
   * 
   * @param ids shortlink ids
   * @return mocker
   */
  public ManagementShortlinkMocker mockShortlinks(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!shortlinkMocker.isMocked(id)) {
          Shortlink shortlink = readShortlinkFromJSONFile(String.format("management/shortlinks/%d.json", id));
          mockShortlink(shortlink);
        } else {
          shortlinkMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management shortlinks
   * 
   * @param ids shortlink ids
   * @return mocker
   */
  public ManagementShortlinkMocker unmockShortlinks(Integer... ids) {
    for (Integer id : ids) {
      shortlinkMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementShortlinkMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    shortlinkMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    shortlinkMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockShortlink(Shortlink shortlink) throws JsonProcessingException {
    Integer shortlinkId = shortlink.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, shortlinkId);
    shortlinkMocker.add(shortlinkId, shortlink, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as shortlink object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Shortlink readShortlinkFromJSONFile(String file) {
    return readJSONFile(file, Shortlink.class);
  }
  
}
