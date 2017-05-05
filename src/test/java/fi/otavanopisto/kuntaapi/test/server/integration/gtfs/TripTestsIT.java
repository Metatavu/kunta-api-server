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
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class TripTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount(String.format("/organizations/%s/transportTrips", getOrganizationId(0)), 7); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteGtfsSettings(organizationId);
  }

  @Test
  public void testListTrips() {
    String organizationId = getOrganizationId(0);
    
    Response response = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportTrips", organizationId);
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(7))
      .body("id[1]", notNullValue())
      .body("routeId[1]", notNullValue())
      .body("scheduleId[1]", notNullValue())
      .body("headsign[1]", is("Mikkeli matkakeskus"));
    
    String routeId = response.body().jsonPath().getString("routeId[1]");
    String scheduleId = response.body().jsonPath().getString("scheduleId[1]");
    
    assertFound(String.format("/organizations/%s/transportRoutes/%s", organizationId, routeId));
    assertFound(String.format("/organizations/%s/transportSchedules/%s", organizationId, scheduleId));
    
  }
  
  @Test
  public void testFindTrip() {
    String organizationId = getOrganizationId(0);
    Response response = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportTrips/{tripId}", organizationId, getOrganizationTripId(organizationId, 2));
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("routeId", notNullValue())
      .body("scheduleId", notNullValue())
      .body("headsign", is("Pajulankylä"));
    
    String routeId = response.body().jsonPath().getString("routeId");
    String scheduleId = response.body().jsonPath().getString("scheduleId");
    
    assertFound(String.format("/organizations/%s/transportRoutes/%s", organizationId, routeId));
    assertFound(String.format("/organizations/%s/transportSchedules/%s", organizationId, scheduleId));
  }
  
  @Test
  public void testOrganizationTripsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationTripId = getOrganizationTripId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/transportTrips/%s", organizationId, organizationTripId));
    assertEquals(7, countApiList(String.format("/organizations/%s/transportTrips", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportTrips/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportTrips/%s", incorrectOrganizationId, organizationTripId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportTrips", incorrectOrganizationId)));
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
