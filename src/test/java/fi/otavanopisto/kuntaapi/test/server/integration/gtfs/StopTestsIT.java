package fi.otavanopisto.kuntaapi.test.server.integration.gtfs;

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
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class StopTestsIT extends AbstractIntegrationTest {
  
  private static final String TIMEZONE = "Europe/Helsinki";
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createGtfsSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/transportStops", getOrganizationId(0)), 5); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteGtfsSettings(organizationId);
  }

  @Test
  public void testListStops() {
    givenReadonly()
      .config(new RestAssuredConfig().jsonConfig(new JsonConfig(JsonPathConfig.NumberReturnType.DOUBLE)))
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
    givenReadonly()
      .config(new RestAssuredConfig().jsonConfig(new JsonConfig(JsonPathConfig.NumberReturnType.DOUBLE)))
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
  
  private void createGtfsSettings(String organizationId) {
    insertOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH, getClass().getClassLoader().getResource("gtfs").getFile());
    insertOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE, TIMEZONE);
    flushCache();
  }
   
  private void deleteGtfsSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH);
    deleteOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE);
  }
}
