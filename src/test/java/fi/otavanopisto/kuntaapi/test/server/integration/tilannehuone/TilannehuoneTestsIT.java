package fi.otavanopisto.kuntaapi.test.server.integration.tilannehuone;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.TilannehuoneConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class TilannehuoneTestsIT extends AbstractIntegrationTest {
  
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
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    String organizationId = getOrganizationId(0);
    
    createSettings(organizationId);
    waitApiListCount(String.format("/organizations/%s/emergencies", organizationId), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteSettings(organizationId);
  }
  
  @Test
  public void testFindEmergency() {
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies/{emergencyId}", organizationId, getOrganizationEmergencyId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("location", is("Helsinki"))
      .body("time", sameInstant(getInstant(2017, 5, 16, 9, 4, TIMEZONE_ID)))
      .body("description", nullValue())
      .body("extent", is("pieni"))
      .body("type", is("tieliikenneonnettomuus"))
      .body("url", is("http://www.tilannehuone.fi/tehtava.php?hash=14857aba356ec79d94c9912d4d72a18c"))
      .body("latitude", is("12.34"))
      .body("longitude", is("34.56"))
      .body("sources.size()", is(1))
      .body("sources[0].name", is("Testi"))
      .body("sources[0].url", is("http://example.com"));
  } 
  
  @Test
  public void testListEmergenciesBySlug() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies?location=Helsinki", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies?location=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  @Test
  public void testListEmergencies() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("location[1]", is("Helsinki"))
      .body("time[1]", sameInstant(getInstant(2017, 5, 16, 8, 39, 1, TIMEZONE_ID)))
      .body("description[1]", nullValue())
      .body("extent[1]", is("keskisuuri"))
      .body("type[1]", is("liikennevälinepalo"))
      .body("url[1]", is("http://www.tilannehuone.fi/tehtava.php?hash=054ac93806eec07464125e5871ee834d"))
      .body("latitude[1]", nullValue())
      .body("longitude[1]", nullValue())
      .body("sources[1].size()", is(0));
  }
  
  @Test
  public void testListEmergenciesSort() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("url[0]", is("http://www.tilannehuone.fi/tehtava.php?hash=14857aba356ec79d94c9912d4d72a18c"))
      .body("url[1]", is("http://www.tilannehuone.fi/tehtava.php?hash=054ac93806eec07464125e5871ee834d"))
      .body("url[2]", is("http://www.tilannehuone.fi/tehtava.php?hash=5f696e25add35ecda4e559651e6b60a1"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies?orderBy=START", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("url[0]", is("http://www.tilannehuone.fi/tehtava.php?hash=5f696e25add35ecda4e559651e6b60a1"))
      .body("url[1]", is("http://www.tilannehuone.fi/tehtava.php?hash=054ac93806eec07464125e5871ee834d"))
      .body("url[2]", is("http://www.tilannehuone.fi/tehtava.php?hash=14857aba356ec79d94c9912d4d72a18c"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies?orderBy=START&orderDir=ASC", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("url[0]", is("http://www.tilannehuone.fi/tehtava.php?hash=5f696e25add35ecda4e559651e6b60a1"))
      .body("url[1]", is("http://www.tilannehuone.fi/tehtava.php?hash=054ac93806eec07464125e5871ee834d"))
      .body("url[2]", is("http://www.tilannehuone.fi/tehtava.php?hash=14857aba356ec79d94c9912d4d72a18c"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/emergencies?orderBy=START&orderDir=DESC", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("url[0]", is("http://www.tilannehuone.fi/tehtava.php?hash=14857aba356ec79d94c9912d4d72a18c"))
      .body("url[1]", is("http://www.tilannehuone.fi/tehtava.php?hash=054ac93806eec07464125e5871ee834d"))
      .body("url[2]", is("http://www.tilannehuone.fi/tehtava.php?hash=5f696e25add35ecda4e559651e6b60a1"));
  }

  @Test
  public void testListEmergenciesFilterByTimes() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/emergencies?before=%s", getIsoDateTime(2017, 5, 16, 9, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("id[0]", notNullValue())
      .body("time[0]", sameInstant(getInstant(2017, 5, 16, 8, 39, 1, TIMEZONE_ID)));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/emergencies?before=%s", getIsoDateTime(2017, 5, 11, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/emergencies?after=%s", getIsoDateTime(2017, 5, 13, 0, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/emergencies?after=%s", getIsoDateTime(2017, 5, 16, 9, 0, TIMEZONE_ID)), getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("time[0]", sameInstant(getInstant(2017, 5, 16, 9, 4, TIMEZONE_ID)));
  } 
  
  @Test
  public void testOrganizationEmergenciesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationEmergencyId = getOrganizationEmergencyId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/emergencies/%s", organizationId, organizationEmergencyId));
    assertEquals(3, countApiList(String.format("/organizations/%s/emergencies", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/emergencies/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/emergencies/%s", incorrectOrganizationId, organizationEmergencyId));
    assertEquals(0, countApiList(String.format("/organizations/%s/emergencies", incorrectOrganizationId)));
  }
    
  private void createSettings(String organizationId) {
    URL url = getClass().getClassLoader().getResource("tilannehuone/tilannehuone.json");
    insertSystemSetting(TilannehuoneConsts.SYSTEM_SETTING_TILANNEHUONE_IMPORT_FILE, url.getFile());
    insertOrganizationSetting(organizationId, TilannehuoneConsts.ORGANIZATION_SETTING_AREA, "Helsingin kaupungin pelastuslaitos");
    flushCache();
  }
   
  private void deleteSettings(String organizationId) {
    deleteSystemSetting(TilannehuoneConsts.SYSTEM_SETTING_TILANNEHUONE_IMPORT_FILE);
    deleteOrganizationSetting(organizationId, TilannehuoneConsts.ORGANIZATION_SETTING_AREA);
  }
  
}
