package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Fragment;

@SuppressWarnings ("squid:S1166")
public class ManagementFragmentMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/wp/v2/fragment";
  
  private ResourceMocker<Integer, Fragment> fragmentMocker = new ResourceMocker<>();

  public ManagementFragmentMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    fragmentMocker.start();
  }
  
  @Override
  public void endMock() {
    fragmentMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management fragments
   * 
   * @param ids fragment ids
   * @return mocker
   */
  public ManagementFragmentMocker mockFragments(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!fragmentMocker.isMocked(id)) {
          Fragment fragment = readFragmentFromJSONFile(String.format("management/fragments/%d.json", id));
          mockFragment(fragment);
        } else {
          fragmentMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management fragments
   * 
   * @param ids fragment ids
   * @return mocker
   */
  public ManagementFragmentMocker unmockFragments(Integer... ids) {
    for (Integer id : ids) {
      fragmentMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementFragmentMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    fragmentMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    fragmentMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockFragment(Fragment fragment) throws JsonProcessingException {
    Integer fragmentId = fragment.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, fragmentId);
    fragmentMocker.add(fragmentId, fragment, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as fragment object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Fragment readFragmentFromJSONFile(String file) {
    return readJSONFile(file, Fragment.class);
  }
  
}
