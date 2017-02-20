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

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

public class StopTimeTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount(String.format("/organizations/%s/transportStopTimes", getOrganizationId(0)), 19); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    deletePtvSettings();
    deleteGtfsSettings(organizationId);
  }
  
  @Test
  public void testListStopTimes() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(19))
      .body("tripId[1]", notNullValue())
      .body("stopId[1]", notNullValue())
      .body("arrivalTime[1]", is(getSecondsFromMidnight("15:06:00")))
      .body("departureTime[1]", is(getSecondsFromMidnight("15:06:00")))
      .body("sequency[1]", is(2));
  }
  
  @Test
  public void testFindStopTime() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportStopTimes/{stopTimeId}", organizationId, getOrganizationStopTimeId(organizationId, 2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("tripId", notNullValue())
      .body("stopId", notNullValue())
      .body("arrivalTime", is(getSecondsFromMidnight("15:07:00")))
      .body("departureTime", is(getSecondsFromMidnight("15:07:00")))
      .body("sequency", is(3));
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
