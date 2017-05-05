package fi.otavanopisto.kuntaapi.test.server.integration.management;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class TileTestsIT extends AbstractIntegrationTest {
  
  private static final String IMAGE_JPEG = "image/jpeg";
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    getManagementMediaMocker()
      .mockMedias(3001, 3002);
  
    getManagementTileMocker()
      .mockTiles(4001, 4002, 4003);
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/tiles", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testFindTiles() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/tiles/{tileId}", organizationId, getTileId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("tile 1"))
      .body("contents", containsString("tile 1 contents"));
  } 
  
  @Test
  public void testListTiles() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/tiles", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("title[1]", is("tile 2"))
      .body("contents[1]", containsString("tile 2 contents"));
  } 
  
  @Test
  public void testTilesNoTitle() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/tiles/{tileId}", organizationId, getTileId(organizationId, 2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is(""))
      .body("contents", is(""));
  } 
  
  @Test
  public void testOrganizationTilesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationTileId = getTileId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/tiles/%s", organizationId, organizationTileId));
    assertEquals(3, countApiList(String.format("/organizations/%s/tiles", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/tiles/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/tiles/%s", incorrectOrganizationId, organizationTileId));
    assertEquals(0, countApiList(String.format("/organizations/%s/tiles", incorrectOrganizationId)));
  }
  
  @Test
  public void testTileImage() {
    String organizationId = getOrganizationId(0);
    String tileId = getTileId(organizationId, 0);
    String imageId = getTileImageId(organizationId, tileId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/tiles/{TILEID}/images/{IMAGEID}", organizationId, tileId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(imageId))
      .body("contentType", is(IMAGE_JPEG));
  }
  
  @Test
  public void testTileImageNotFound() {
    String organizationId = getOrganizationId(0);
    String tileId = getTileId(organizationId, 0);
    String incorrectTileId = getTileId(organizationId, 2);
    String imageId = getTileImageId(organizationId, tileId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/tiles/%s/images/%s", organizationId, tileId, imageId));
    assertEquals(1, countApiList(String.format("/organizations/%s/tiles/%s/images", organizationId, tileId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/tiles/%s/images/%s", organizationId, tileId, malformedId));
    }

    assertNotFound(String.format("/organizations/%s/tiles/%s/images/%s", organizationId, incorrectTileId, imageId));
    assertEquals(0, countApiList(String.format("/organizations/%s/tiles/%s/images", organizationId, incorrectTileId)));
  }

  @Test
  public void testTileImageData() {
    String organizationId = getOrganizationId(0);
    String tileId = getTileId(organizationId, 0);
    String imageId = getTileImageId(organizationId, tileId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/tiles/{TILEID}/images/{IMAGEID}/data", organizationId, tileId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "21621")
      .header("Content-Type", IMAGE_JPEG);
  }
  
  @Test
  public void testTileImageDataScaled() {
    String organizationId = getOrganizationId(0);
    String tileId = getTileId(organizationId, 0);
    String imageId = getTileImageId(organizationId, tileId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/tiles/{TILEID}/images/{IMAGEID}/data?size=100", organizationId, tileId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "2033")
      .header("Content-Type", IMAGE_JPEG);
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
