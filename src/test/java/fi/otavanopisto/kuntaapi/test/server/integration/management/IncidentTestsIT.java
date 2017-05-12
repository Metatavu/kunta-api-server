package fi.otavanopisto.kuntaapi.test.server.integration.management;

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

import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class IncidentTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    getManagementIncidentMocker()
      .mockIncidents(10001, 10002, 10003);
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/incidents", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testFindIncidents() {
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/incidents/{incidentId}", organizationId, getOrganizationIncidentId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("slug", is("toimimattomat-puhelinyhteydet"))
      .body("title", is("Toimimattomat puhelinyhteydet"))
      .body("description", is("Puhelin ei toimi Rantasalmella"))
      .body("start", sameInstant(getInstant(2017, 5, 29, 0, 0, TIMEZONE_ID)))
      .body("end", sameInstant(getInstant(2017, 5, 30, 0, 0, TIMEZONE_ID)))
      .body("areas.size()", is(1))
      .body("areas[0]", is("Rantasalmi"))
      .body("severity", is("disruption"));
  } 
  
  @Test
  public void testListIncidentsBySlug() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/incidents?slug=karhu", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("karhu"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/incidents?slug=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  @Test
  public void testListIncidents() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/incidents", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("slug[1]", is("kemikaalivuoto"))
      .body("title[1]", is("Kemikaalivuoto"))
      .body("description[1]", is("Vaarallisia kemikaaleja vuotanut Mikkelin torille"))
      .body("start[1]", sameInstant(getInstant(2017, 5, 26, 15, 0, TIMEZONE_ID)))
      .body("end[1]", sameInstant(getInstant(2017, 5, 26, 16, 0, TIMEZONE_ID)))
      .body("areas[1].size()", is(1))
      .body("areas[1][0]", is("Mikkelin kaupunki"))
      .body("severity[1]", is("severe"));
  }
  
  @Test
  public void testListIncidentsFilterByTimes() {
    getInstant(2017, 5, 26, 15, 0, TIMEZONE_ID);
    
//    1
//    "start_time": "2017-05-29T00:00:00",
//    "end_time": "2017-05-30T00:00:00",
//    2
//    "start_time": "2017-05-26T15:00:00",
//    "end_time": "2017-05-26T16:00:00",
//    3
//    "start_time": "2017-05-12T00:00:00",
//    "end_time": "2017-05-14T00:00:00",
    
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/incidents?startBefore=%s", getIsoDateTime(2017, 5, 13, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("karhu"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/incidents?startBefore=%s", getIsoDateTime(2017, 5, 11, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/incidents?endAfter=%s", getIsoDateTime(2017, 5, 13, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/incidents?endAfter=%s", getIsoDateTime(2017, 5, 29, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("slug[0]", is("toimimattomat-puhelinyhteydet"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/incidents?startBefore=%s", getIsoDateTime(2016, 6, 1, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testOrganizationIncidentsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationIncidentId = getOrganizationIncidentId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/incidents/%s", organizationId, organizationIncidentId));
    assertEquals(3, countApiList(String.format("/organizations/%s/incidents", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/incidents/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/incidents/%s", incorrectOrganizationId, organizationIncidentId));
    assertEquals(0, countApiList(String.format("/organizations/%s/incidents", incorrectOrganizationId)));
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
