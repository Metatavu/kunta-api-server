package fi.otavanopisto.kuntaapi.test.server.integration.gtfs;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ScheduleTestsIT extends AbstractIntegrationTest{

  private static final String TIMEZONE = "Europe/Helsinki";
  
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
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createGtfsSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/transportSchedules", getOrganizationId(0)), 17); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    deletePtvSettings();
    deleteGtfsSettings(organizationId);
  }
  
  @Test
  public void testListSchedules() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportSchedules", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(17))
      .body("id[1]", notNullValue())
      .body("name[1]", is("L+ Mikkeli"))
      .body("days[1].size()", is(1))
      .body("days[1][0]", is(6))
      .body("exceptions[1].size()", is(10))
      .body("exceptions[1][1].type", is("REMOVE"))
      .body("exceptions[1][1].date",sameInstant(getInstant(2016, 7, 30, 0, 0, ZoneId.of(TIMEZONE))))
      .body("startDate[1]", sameInstant(getInstant(2016, 5, 1, 0, 0, ZoneId.of(TIMEZONE))))
      .body("endDate[1]", sameInstant(getInstant(2024, 4, 30, 0, 0, ZoneId.of(TIMEZONE))));
  }
  
  @Test
  public void testFindSchedule() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportSchedules/{scheduleId}", organizationId, getOrganizationScheduleId(organizationId, 10))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", is("Ma+++"))
      .body("days.size()", is(1))
      .body("days[0]", is(1))
      .body("exceptions.size()", is(22))
      .body("exceptions[1].type", is("REMOVE"))
      .body("exceptions[1].date",sameInstant(getInstant(2017, 1, 23, 0, 0, ZoneId.of(TIMEZONE))))
      .body("startDate", sameInstant(getInstant(2016, 5, 1, 0, 0, ZoneId.of(TIMEZONE))))
      .body("endDate", sameInstant(getInstant(2024, 4, 30, 0, 0, ZoneId.of(TIMEZONE))));
  }
  
  @Test
  public void testOrganizationSchedulesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationScheduleId = getOrganizationScheduleId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/organizations/%s/transportSchedules/%s", organizationId, organizationScheduleId));
    assertEquals(17, countApiList(String.format("/organizations/%s/transportSchedules", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportSchedules/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportSchedules/%s", incorrectOrganizationId, organizationScheduleId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportSchedules", incorrectOrganizationId)));
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
    insertOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE, TIMEZONE);
    flushCache();
  }
   
  private void deleteGtfsSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_PATH);
    deleteOrganizationSetting(organizationId, GtfsConsts.ORGANIZATION_SETTING_GTFS_TIMEZONE);
  }
}
