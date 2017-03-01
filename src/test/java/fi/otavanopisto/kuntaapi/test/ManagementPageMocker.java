package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

import fi.metatavu.management.client.model.Page;

public class ManagementPageMocker extends AbstractManagementMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/pages";
  
  private Map<Integer, ManagementMockedEntity<Page>> mockedPages = new HashMap<>();
  private MappingBuilder listMapping;

  @Override
  public void startMock() {
    super.startMock();
    startMocks();
  }
  
  @Override
  public void endMock() {
    unmockPageList();
    
    for (ManagementMockedEntity<Page> mockedPage : mockedPages.values()) {
      removeStub(mockedPage.getEntityMapping());
      removeStub(mockedPage.getNotFoundMapping());
    }
    
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
        Page page = readPageFromJSONFile(String.format("management/pages/%d.json", id));
        mockPage(page);
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    if (isMocking()) {
      startMocks();
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
      ManagementMockedEntity<Page> mockedPage = mockedPages.remove(id);
      if (isMocking() && mockedPage != null) {
        removeStub(mockedPage.getEntityMapping());
        stubFor(mockedPage.getNotFoundMapping());
      }
    }
    
    if (isMocking()) {
      mockPageList();
    }
    
    return this;
  }

  private void startMocks() {
    for (ManagementMockedEntity<Page> mockedPage : mockedPages.values()) {
      stubFor(mockedPage.getEntityMapping());
    }
    
    mockPageList();
  }
  
  private void mockPageList() {
    unmockPageList();
    
    try {
      List<Page> pages = new ArrayList<>();
      for (ManagementMockedEntity<Page> mockedPage : mockedPages.values()) {
        pages.add(mockedPage.getEntity());
      }
      
      MappingBuilder mappingBuilder = get(urlPathEqualTo(PAGES_PATH));
    
      listMapping = mappingBuilder
        .willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(toJSON(pages)));
      
      stubFor(listMapping);
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
  }
  
  private void unmockPageList() {
    if (listMapping != null) {
      removeStub(listMapping);
    }
  }
  
  private void mockPage(Page page) throws JsonProcessingException {
    Integer pageId = page.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, pageId);
    
    MappingBuilder mappingBuilder = get(urlPathEqualTo(path));
    
    MappingBuilder pageMapping = mappingBuilder
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, APPLICATION_JSON)
      .withBody(toJSON(page)));
    
    mockedPages.put(pageId, new ManagementMockedEntity<Page>(page, pageMapping, createNotFoundMapping(path)));
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
