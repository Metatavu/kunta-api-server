package fi.otavanopisto.kuntaapi.test.server.integration.management;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class BannerTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createPtvSettings();
    
    getPtvMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e")
      .startMock();
    
    getManagementMocker()
      .mockBanners("2001", "2002", "2003")
      .startMock();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/banners", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    getManagementMocker().endMock();
    deletePtvSettings();
    deleteManagementSettings(organizationId);
    deleteAllBanners();
  }
  
  @Test
  public void testFindBanners() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/banners/{bannerId}", organizationId, getOrganizationBannerId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("Banner 1"))
      .body("contents", containsString("Banner 1 contains text"))
      .body("link", nullValue())
      .body("textColor", nullValue())
      .body("backgroundColor", nullValue());
  } 
  
  @Test
  public void testListBanners() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/banners", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("title[2]", is("Banner 3 &#8211; the full featured one"))
      .body("contents[2]", is(""))
      .body("link[2]", is("http://link.example.com"))
      .body("textColor[2]", is("#e03339"))
      .body("backgroundColor[2]", is("rgba(221,153,51,0.3)"));
  } 
  
  @Test
  public void testBannersNoTitle() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/banners/{bannerId}", organizationId, getOrganizationBannerId(organizationId, 1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", nullValue())
      .body("contents", is(""))
      .body("link", nullValue())
      .body("textColor", nullValue())
      .body("backgroundColor", nullValue());
  } 
  
  @Test
  public void testOrganizationBannersNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationAnnouncecmentId = getOrganizationBannerId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/banners/%s", organizationId, organizationAnnouncecmentId));
    assertEquals(3, countApiList(String.format("/organizations/%s/banners", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/banners/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/banners/%s", incorrectOrganizationId, organizationAnnouncecmentId));
    assertEquals(0, countApiList(String.format("/organizations/%s/banners", incorrectOrganizationId)));
  }

  private void createPtvSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
  
  private void deletePtvSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}