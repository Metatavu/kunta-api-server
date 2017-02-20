package fi.otavanopisto.kuntaapi.test.server.integration.gtfs;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.config.JsonConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.config.JsonPathConfig;

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

public class StopTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount("/organizations", 1);
    
    createGtfsSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/transportStops", getOrganizationId(0)), 5); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    deletePtvSettings();
    deleteGtfsSettings(organizationId);
  }

  @Test
  public void testListStops() {
    given()
      .config(new RestAssuredConfig().jsonConfig(new JsonConfig(JsonPathConfig.NumberReturnType.DOUBLE)))
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStops", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(5))
      .body("id[1]", notNullValue())
      .body("name[1]", is("Vanhala koulu"))
      .body("lat[1]", is(61.7696611577961))
      .body("lng[1]", is(27.1201016871884));
  }
  
  @Test
  public void testFindStop() {
    String organizationId = getOrganizationId(0);
    given()
      .config(new RestAssuredConfig().jsonConfig(new JsonConfig(JsonPathConfig.NumberReturnType.DOUBLE)))
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStops/{stopId}", organizationId, getOrganizationStopId(organizationId, 2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", is("Tenholahdentie  P"))
      .body("lat", is(61.7005145626563))
      .body("lng", is(27.2056931881733));

  }
  
  @Test
  public void testOrganizationStopsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationStopId = getOrganizationStopId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/transportStops/%s", organizationId, organizationStopId));
    assertEquals(5, countApiList(String.format("/organizations/%s/transportStops", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportStops/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportStops/%s", incorrectOrganizationId, organizationStopId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportStops", incorrectOrganizationId)));
  }

  private void createPtvSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
  
  private void deletePtvSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
  private void createGtfsSettings(String organizationId) {
    insertOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH, getClass().getClassLoader().getResource("gtfs").getFile());
    flushCache();
  }
   
  private void deleteGtfsSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH);
  }
}
