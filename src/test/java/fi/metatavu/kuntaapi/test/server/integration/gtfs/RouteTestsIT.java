package fi.metatavu.kuntaapi.test.server.integration.gtfs;

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

import fi.metatavu.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class RouteTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount(String.format("/organizations/%s/transportRoutes", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteGtfsSettings(organizationId);
  }

  @Test
  public void testListRoutes() {
    String organizationId = getOrganizationId(0);
    
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportRoutes", organizationId);
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("shortName[1]", is("1 A"))
      .body("longName[1]", is("Tori-Tuppurala-Tori"))
      .body("agencyId[1]", notNullValue());
    
    String agencyId = response.body().jsonPath().getString("agencyId[1]");
    assertFound(String.format("/organizations/%s/transportAgencies/%s", organizationId, agencyId));
  }
  
  @Test
  public void testFindRoute() {
    String organizationId = getOrganizationId(0);
    Response response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportRoutes/{routeId}", organizationId, getOrganizationRouteId(organizationId, 2));
    
    response
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("shortName", is("1 AB"))
      .body("longName", is("Tori-Tuppurala-Tori"))
      .body("agencyId", notNullValue());
    
    String agencyId = response.body().jsonPath().getString("agencyId");
    assertFound(String.format("/organizations/%s/transportAgencies/%s", organizationId, agencyId));
  }
  
  @Test
  public void testOrganizationRoutesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationRouteId = getOrganizationRouteId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/transportRoutes/%s", organizationId, organizationRouteId));
    assertEquals(3, countApiList(String.format("/organizations/%s/transportRoutes", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportRoutes/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportRoutes/%s", incorrectOrganizationId, organizationRouteId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportRoutes", incorrectOrganizationId)));
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