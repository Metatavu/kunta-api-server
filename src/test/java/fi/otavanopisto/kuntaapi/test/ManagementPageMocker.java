package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Page;

@SuppressWarnings ("squid:S1166")
public class ManagementPageMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/pages";
  
  private ManagementResourceMocker<Integer, Page> pageMocker = new ManagementResourceMocker<>();

  public ManagementPageMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    pageMocker.start();
  }
  
  @Override
  public void endMock() {
    pageMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management pages
   * 
   * @param ids page ids
   * @return mocker
   */
  public ManagementPageMocker mockPages(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!pageMocker.isMocked(id)) {
          Page page = readPageFromJSONFile(String.format("management/pages/%d.json", id));
          mockPage(page);
        } else {
          pageMocker.setStatus(id, MockedResourceStatus.OK);
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
  public ManagementPageMocker mockAlternative(Integer id, String alternative) {
    Page page = readPageFromJSONFile(String.format("management/pages/%d_%s.json", id, alternative));
    pageMocker.mockAlternative(id, page);
  
    return this;
  }
  
  /**
   * Unmocks management pages
   * 
   * @param ids page ids
   * @return mocker
   */
  public ManagementPageMocker unmockPages(Integer... ids) {
    for (Integer id : ids) {
      pageMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementPageMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("50"));
    queryParams.put("page", containing("1"));
    
    pageMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    pageMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockPage(Page page) throws JsonProcessingException {
    Integer pageId = page.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, pageId);
    pageMocker.add(pageId, page, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as page object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Page readPageFromJSONFile(String file) {
    return readJSONFile(file, Page.class);
  }
  
}
