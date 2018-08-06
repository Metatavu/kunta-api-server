package fi.metatavu.kuntaapi.test.server.integration.gtfs;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;

import fi.metatavu.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class StopTimeTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount(String.format("/organizations/%s/transportStopTimes", getOrganizationId(0)), 19); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteGtfsSettings(organizationId);
  }
  
  @Test
  public void testListStopTimes() {
    String organizationId = getOrganizationId(0);
    
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes", organizationId);
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(19))
      .body("tripId[1]", notNullValue())
      .body("stopId[1]", notNullValue())
      .body("arrivalTime[1]", is(getSecondsFromMidnight("15:06:00")))
      .body("departureTime[1]", is(getSecondsFromMidnight("15:06:00")))
      .body("sequency[1]", is(2));
    
    String tripId = response.body().jsonPath().getString("tripId[1]");
    String stopId = response.body().jsonPath().getString("stopId[1]");
    
    assertFound(String.format("/organizations/%s/transportTrips/%s", organizationId, tripId));
    assertFound(String.format("/organizations/%s/transportStops/%s", organizationId, stopId));
  }
  
  @Test
  public void testListStopTimesDepartureTime() {
    String organizationId = getOrganizationId(0);
    int departureTime = getSecondsFromMidnight("15:34:00");
    
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/transportStopTimes?departureTime=%d", departureTime), organizationId);
    
    ValidatableResponse validatableResponse = response
      .then()
      .assertThat()
      .statusCode(200);
    
    validatableResponse.body("id.size()", is(9));

    int count = response
      .body()
      .jsonPath()
      .getInt("id.size()");

    for (int i = 0; i < count; i++) {
      validatableResponse.body(String.format("departureTime[%d]", i), greaterThanOrEqualTo(departureTime));
    }
  }
  
  @Test
  public void testListStopTimesStopId() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    waitApiListCount(String.format("/organizations/%s/transportStops", organizationId), 5); 
    
    String stopId = getOrganizationStopId(organizationId, 0);
    
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/transportStopTimes?stopId=%s", stopId), organizationId);
    
    ValidatableResponse validatableResponse = response
      .then()
      .assertThat()
      .statusCode(200);
    
    validatableResponse.body("id.size()", is(7));

    int count = response
      .body()
      .jsonPath()
      .getInt("id.size()");

    for (int i = 0; i < count; i++) {
      validatableResponse.body(String.format("stopId[%d]", i), is(stopId));
    }
  }
  
  @Test
  public void testListStopTimesSortByDepartureTime() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes?sortBy=DEPARTURE_TIME", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(19))
      .body("departureTime[1]", is(getSecondsFromMidnight("15:06:00"))); 
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes?sortBy=DEPARTURE_TIME&sortDir=DESC", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(19))
      .body("departureTime[1]", is(getSecondsFromMidnight("15:41:00"))); 
  }
  
  @Test
  public void testFindStopTime() {
    String organizationId = getOrganizationId(0);
    
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes/{stopTimeId}", organizationId, getOrganizationStopTimeId(organizationId, 2));
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("tripId", notNullValue())
      .body("stopId", notNullValue())
      .body("arrivalTime", is(getSecondsFromMidnight("15:07:00")))
      .body("departureTime", is(getSecondsFromMidnight("15:07:00")))
      .body("sequency", is(3));
    
    String tripId = response.body().jsonPath().getString("tripId");
    String stopId = response.body().jsonPath().getString("stopId");
    
    assertFound(String.format("/organizations/%s/transportTrips/%s", organizationId, tripId));
    assertFound(String.format("/organizations/%s/transportStops/%s", organizationId, stopId));
  }
  
  @Test
  public void testOrganizationStopTimesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationStopTimeId = getOrganizationStopTimeId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/transportStopTimes/%s", organizationId, organizationStopTimeId));
    assertEquals(19, countApiList(String.format("/organizations/%s/transportStopTimes", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportStopTimes/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportStopTimes/%s", incorrectOrganizationId, organizationStopTimeId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportStopTimes", incorrectOrganizationId)));
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