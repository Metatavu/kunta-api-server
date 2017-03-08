package fi.otavanopisto.kuntaapi.test.server.integration.management;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;

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
public class PostTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.systemDefault();
  private static final String IMAGE_JPEG = "image/jpeg";
  private static final String IMAGE_PNG = "image/png";
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createPtvSettings();

    getRestfulPtvOrganizationMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e");
    
    getManagementPostMocker()
      .mockPosts(789, 890, 901);
    
    getManagementMocker()
      .mockMedia("3001", "3002")
      .startMock();
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/news", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deletePtvSettings();
    deleteManagementSettings(organizationId);
    getPtvMocker().endMock();
    getManagementMocker().endMock();
  }

  @Test
  public void testFindPosts() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news/{newsArticleId}", organizationId, getNewsArticleId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("Lorem ipsum dolor sit amet"))
      .body("slug", is("lorem-ipsum-dolor-sit-amet"))
      .body("abstract", containsString("Consectetur adipiscing elit"))
      .body("contents", containsString("Aenean a pellentesque erat"))
      .body("published", sameInstant(getInstant(2017, 01, 12, 13, 38, TIMEZONE_ID)));
  }
  
  @Test
  public void testMoreLink() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news/{newsArticleId}", organizationId, getNewsArticleId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("abstract", containsString("Consectetur adipiscing elit"))
      .body("abstract", not(containsString("more-link")));
  }
  
  @Test
  public void testListPosts() {
    // Zeus shoud be listed before abraham and bertha because it's menu_order is set to -100 

    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("slug[0]", is("lorem-ipsum-dolor-sit-amet"))
      .body("slug[1]", is("test-2"))
      .body("slug[2]", is("test-3"));
  } 
  
  @Test
  public void testListPostsByPath() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?slug=test-3", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("test-3")); 
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?slug=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
    
  @Test
  public void testOrganizationPostsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationNewsArticleId = getNewsArticleId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/news/%s", organizationId, organizationNewsArticleId));
    assertEquals(3, countApiList(String.format("/organizations/%s/news", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/news/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/news/%s", incorrectOrganizationId, organizationNewsArticleId));
    assertEquals(0, countApiList(String.format("/organizations/%s/news", incorrectOrganizationId)));
  }
  
  @Test
  public void testNewsArticleImage() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{NEWSARTICLEID}/images/{IMAGEID}", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(imageId))
      .body("contentType", is(IMAGE_JPEG));
  }
  
  @Test
  public void testNewsArticleImageNotFound() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String incorrectNewsArticleId = getNewsArticleId(organizationId, 2);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, newsArticleId, imageId));
    assertEquals(1, countApiList(String.format("/organizations/%s/news/%s/images", organizationId, newsArticleId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, newsArticleId, malformedId));
    }

    assertNotFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, incorrectNewsArticleId, imageId));
    assertEquals(0, countApiList(String.format("/organizations/%s/news/%s/images", organizationId, incorrectNewsArticleId)));
  }

  @Test
  public void testNewsArticleImageData() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{NEWSARTICLEID}/images/{IMAGEID}/data", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "21621")
      .header("Content-Type", IMAGE_JPEG);
  }
  
  @Test
  public void testNewsArticleImageDataScaled() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{EVENTID}/images/{IMAGEID}/data?size=100", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "13701")
      .header("Content-Type", IMAGE_PNG);
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
