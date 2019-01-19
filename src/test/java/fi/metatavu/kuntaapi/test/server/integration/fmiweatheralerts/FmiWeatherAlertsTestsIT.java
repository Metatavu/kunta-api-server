package fi.metatavu.kuntaapi.test.server.integration.fmiweatheralerts;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.FmiWeatherAlertsConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;
import fi.metatavu.kuntaapi.test.server.integration.ptv.TestPtvConsts;

/**
 * Tests for FMI weather alerts
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public class FmiWeatherAlertsTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Z");
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock(TestPtvConsts.ORGANIZATIONS[2]);
    
    getFmiWeatherAlertsWfsMocker().mockWfs("/fmiweatheralerts", "fmi/weatheralerts/wfs.json");
    
    startMocks();
    
    waitApiListCount("/organizations", 1);
    String organizationId = getOrganizationId(0);
    
    createFmiSettings(organizationId);

    waitApiListCount(String.format("/organizations/%s/environmentalWarnings", organizationId), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteFmiSettings(organizationId);
  }
  
  @Test
  public void findEnvironmentalWarning() {
    String organizationId = getOrganizationId(0);
    String environmentalWarningId = getEnvironmentalWarningId(organizationId, 0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings/{environmentalWarningId}", organizationId, environmentalWarningId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("WEATHER"))
      .body("context", is("sea-wind"))
      .body("severity", is("level-2"))
      .body("description.size()", is(3))
      .body("description[0].language", is("en"))
      .body("description[0].value", is("Example wind"))
      .body("description[0].type", nullValue())
      .body("description[1].language", is("fi"))
      .body("description[1].value", is("Esimerkki tuuli"))
      .body("description[1].type", nullValue())
      .body("description[2].language", is("sv"))
      .body("description[2].value", is("Exempel vind"))
      .body("description[2].type", nullValue())
      .body("causes.size()", is(0))
      .body("actualizationProbability", is(30f))
      .body("start", sameInstant(getInstant(2019, 2, 12, 3, 0, TIMEZONE_ID)))
      .body("end", sameInstant(getInstant(2019, 2, 12, 4, 0, TIMEZONE_ID)));
  }
  
  @Test
  public void listEnvironmentalWarning() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("type[2]", is("WEATHER"))
      .body("context[2]", is("traffic-weather"))
      .body("severity[2]", is("level-2"))
      .body("description[2].size()", is(3))
      .body("description[2][0].language", is("en"))
      .body("description[2][0].value", is("Example with ä and ö characters"))
      .body("description[2][0].type", nullValue())
      .body("description[2][1].language", is("fi"))
      .body("description[2][1].value", is("Esimerkki, jossa on mukana myös ä ja ö -kirjaimia"))
      .body("description[2][1].type", nullValue())
      .body("description[2][2].language", is("sv"))
      .body("description[2][2].value", is("Exempel med ä och ö tecken"))
      .body("description[2][2].type", nullValue())
      .body("causes[2].size()", is(1))
      .body("causes[2][0]", is("snow"))
      .body("actualizationProbability[2]", is(60f))
      .body("start[2]", sameInstant(getInstant(2019, 1, 18, 12, 0, TIMEZONE_ID)))
      .body("end[2]", sameInstant(getInstant(2019, 1, 18, 18, 0, TIMEZONE_ID)));
  }
  
  @Test
  public void listEnvironmentalWarningByStart() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?startBefore=2019-01-19T00:00:00Z", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("description[0][0].value", is("Example with ä and ö characters"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?startBefore=2019-01-19T00:00:00Z&startAfter=2019-01-17T12:00:00Z", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("description[0][0].value", is("Example with ä and ö characters"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?startAfter=2019-01-19T12:00:00Z", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("description[0][0].value", is("Example wind"))
      .body("description[1][0].value", is("Example snow"));
  }

  @Test
  public void listEnvironmentalWarningByContext() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?contexts=traffic-weather", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("description[0][0].value", is("Example snow"))
      .body("description[1][0].value", is("Example with ä and ö characters"));
  }
  
  @Test
  public void listEnvironmentalWarningSort() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?sortBy=START&sortDir=ASC", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("description[0][0].value", is("Example wind"))
      .body("description[1][0].value", is("Example snow"))
      .body("description[2][0].value", is("Example with ä and ö characters"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/environmentalWarnings?sortBy=START&sortDir=DESC", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("description[2][0].value", is("Example wind"))
      .body("description[1][0].value", is("Example snow"))
      .body("description[0][0].value", is("Example with ä and ö characters"));
  } 
  
  @Test
  public void testEnvironmentalWarningsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationEnvironmentalWarningId = getEnvironmentalWarningId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/environmentalWarnings/%s", organizationId, organizationEnvironmentalWarningId));
    assertEquals(3, countApiList(String.format("/organizations/%s/environmentalWarnings", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/environmentalWarnings/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/environmentalWarnings/%s", incorrectOrganizationId, organizationEnvironmentalWarningId));
    assertEquals(0, countApiList(String.format("/organizations/%s/environmentalWarnings", incorrectOrganizationId)));
  }
    
  /**
   * Creates test settings
   * 
   * @param organizationId organization id
   */
  private void createFmiSettings(String organizationId) {
    insertSystemSetting(FmiWeatherAlertsConsts.SYSTEM_SETTING_API_URL, String.format("%s/fmiweatheralerts", getWireMockBasePath(), BASE_URL));
    insertOrganizationSetting(organizationId, FmiWeatherAlertsConsts.ORGANIZATION_SETTING_REFERENCE, "http://example.com/county.xml#county.2");
    flushCache();
  }
   
  /**
   * Cleans test settings
   * 
   * @param organizationId organization id
   */
  private void deleteFmiSettings(String organizationId) {
    deleteSystemSetting(FmiWeatherAlertsConsts.SYSTEM_SETTING_API_URL);
    deleteOrganizationSetting(organizationId, FmiWeatherAlertsConsts.ORGANIZATION_SETTING_REFERENCE);
  }
  
}
