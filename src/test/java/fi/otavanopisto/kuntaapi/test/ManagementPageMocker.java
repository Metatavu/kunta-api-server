package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.metatavu.management.client.model.Page;

@SuppressWarnings ("squid:S1166")
public class ManagementPageMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/pages";
  
  private ResourceMocker<Integer, Page> pageMocker = new ResourceMocker<>();

  public ManagementPageMocker() {
    mockList();
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
  
  public ManagementPageMocker mockList() {
    pageMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
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
