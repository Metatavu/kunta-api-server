package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Banner;

@SuppressWarnings ("squid:S1166")
public class ManagementBannerMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/wp/v2/banner";
  
  private ManagementResourceMocker<Integer, Banner> bannerMocker = new ManagementResourceMocker<>();

  public ManagementBannerMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    bannerMocker.start();
  }
  
  @Override
  public void endMock() {
    bannerMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management banners
   * 
   * @param ids banner ids
   * @return mocker
   */
  public ManagementBannerMocker mockBanners(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!bannerMocker.isMocked(id)) {
          Banner banner = readBannerFromJSONFile(String.format("management/banners/%d.json", id));
          mockBanner(banner);
        } else {
          bannerMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management banners
   * 
   * @param ids banner ids
   * @return mocker
   */
  public ManagementBannerMocker unmockBanners(Integer... ids) {
    for (Integer id : ids) {
      bannerMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementBannerMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    bannerMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    bannerMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockBanner(Banner banner) throws JsonProcessingException {
    Integer bannerId = banner.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, bannerId);
    bannerMocker.add(bannerId, banner, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as banner object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Banner readBannerFromJSONFile(String file) {
    return readJSONFile(file, Banner.class);
  }
  
}
