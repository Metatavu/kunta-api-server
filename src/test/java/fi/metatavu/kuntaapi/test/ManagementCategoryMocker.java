package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Category;

@SuppressWarnings ("squid:S1166")
public class ManagementCategoryMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/categories";
  
  private ManagementResourceMocker<Integer, Category> categoryMocker = new ManagementResourceMocker<>();

  public ManagementCategoryMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    categoryMocker.start();
  }
  
  @Override
  public void endMock() {
    categoryMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management categories
   * 
   * @param ids category ids
   * @return mocker
   */
  public ManagementCategoryMocker mockCategories(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!categoryMocker.isMocked(id)) {
          Category category = readCategoryFromJSONFile(String.format("management/categories/%d.json", id));
          mockCategory(category);
        } else {
          categoryMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management categories
   * 
   * @param ids category ids
   * @return mocker
   */
  public ManagementCategoryMocker unmockCategories(Integer... ids) {
    for (Integer id : ids) {
      categoryMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementCategoryMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    categoryMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    categoryMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockCategory(Category category) throws JsonProcessingException {
    Integer categoryId = category.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, categoryId);
    categoryMocker.add(categoryId, category, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as category object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Category readCategoryFromJSONFile(String file) {
    return readJSONFile(file, Category.class);
  }
  
}
