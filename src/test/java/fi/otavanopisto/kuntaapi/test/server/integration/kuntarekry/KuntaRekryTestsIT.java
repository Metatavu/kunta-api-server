package fi.otavanopisto.kuntaapi.test.server.integration.kuntarekry;

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

import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class KuntaRekryTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  
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
    
    getKuntarekryMocker()
      .mockKuntaRekryFeed("/kuntarekry", "kuntarekry/feed.xml")
      .startMock();
    
    waitApiListCount("/organizations", 1);
    
    createKuntaRekrySettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/jobs", getOrganizationId(0)), 3); 
    
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    deletePtvSettings();
    deleteKuntaRekrySettings(organizationId);
  }
  
  @Test
  public void findListJob() {
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs/{jobId}", organizationId, getOrganizationJobId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("Työn 1 otsikko"))
      .body("employmentType", is("Keikkatyö"))
      .body("description", is("Esimerkkityön 1 kuvaus"))
      .body("location", is("Esimerkki"))
      .body("organisationalUnit", is("Esimerkin yksikkö"))
      .body("duration", is("Vuorotyö"))
      .body("taskArea", is("Lähi- ja perushoitajat"))
      .body("publicationStart", sameInstant(getInstant(2010, 1, 2, 0, 0, TIMEZONE_ID)))
      .body("publicationEnd",  sameInstant(getInstant(2020, 1, 2, 0, 0, TIMEZONE_ID)))
      .body("link", is("https://www.kuntarekry.fi/fi/tyopaikka/1234567890"));
  }
  
  @Test
  public void testListJobs() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("title[1]", is("Työn 2 otsikko"))
      .body("employmentType[1]", is("Keikkatyö"))
      .body("description[1]", is("Esimerkkityön 2 kuvaus"))
      .body("location[1]", is("Esimerkki"))
      .body("organisationalUnit[1]", is("Esimerkin yksikkö"))
      .body("duration[1]", is("Vuorotyö"))
      .body("taskArea[1]", is("Lähi- ja perushoitajat"))
      .body("publicationStart[1]", sameInstant(getInstant(2111, 1, 2, 0, 0, TIMEZONE_ID)))
      .body("publicationEnd[1]",  sameInstant(getInstant(2121, 1, 2, 0, 0, TIMEZONE_ID)))
      .body("link[1]", is("https://www.kuntarekry.fi/fi/tyopaikka/2234567890"));
  } 
  
  @Test
  public void testListJobsSort() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_END&sortDir=ASCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("link[2]", is("https://www.kuntarekry.fi/fi/tyopaikka/2234567890"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_END&sortDir=DESCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("link[0]", is("https://www.kuntarekry.fi/fi/tyopaikka/2234567890"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_START&sortDir=ASCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("link[2]", is("https://www.kuntarekry.fi/fi/tyopaikka/2234567890"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_START&sortDir=DESCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("link[0]", is("https://www.kuntarekry.fi/fi/tyopaikka/2234567890"));
  } 
  
  @Test
  public void testOrganizationJobsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationJobId = getOrganizationJobId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/jobs/%s", organizationId, organizationJobId));
    assertEquals(3, countApiList(String.format("/organizations/%s/jobs", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/jobs/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/jobs/%s", incorrectOrganizationId, organizationJobId));
    assertEquals(0, countApiList(String.format("/organizations/%s/jobs", incorrectOrganizationId)));
  }

  
  private void createPtvSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
  
  private void deletePtvSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
    
  private void createKuntaRekrySettings(String organizationId) {
    insertOrganizationSetting(organizationId, KuntaRekryConsts.ORGANIZATION_SETTING_APIURI, String.format("%s/kuntarekry", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteKuntaRekrySettings(String organizationId) {
    deleteOrganizationSetting(organizationId, KuntaRekryConsts.ORGANIZATION_SETTING_APIURI);
  }
  
}
