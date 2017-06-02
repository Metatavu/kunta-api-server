package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Tag;

@SuppressWarnings ("squid:S1166")
public class ManagementTagMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/tags";
  
  private ManagementResourceMocker<Integer, Tag> tagMocker = new ManagementResourceMocker<>();

  public ManagementTagMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    tagMocker.start();
  }
  
  @Override
  public void endMock() {
    tagMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management tags
   * 
   * @param ids tag ids
   * @return mocker
   */
  public ManagementTagMocker mockTags(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!tagMocker.isMocked(id)) {
          Tag tag = readTagFromJSONFile(String.format("management/tags/%d.json", id));
          mockTag(tag);
        } else {
          tagMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management tags
   * 
   * @param ids tag ids
   * @return mocker
   */
  public ManagementTagMocker unmockTags(Integer... ids) {
    for (Integer id : ids) {
      tagMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementTagMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    tagMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    tagMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockTag(Tag tag) throws JsonProcessingException {
    Integer tagId = tag.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, tagId);
    tagMocker.add(tagId, tag, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as tag object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Tag readTagFromJSONFile(String file) {
    return readJSONFile(file, Tag.class);
  }
  
}
