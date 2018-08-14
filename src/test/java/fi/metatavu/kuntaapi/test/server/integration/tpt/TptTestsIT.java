package fi.metatavu.kuntaapi.test.server.integration.tpt;

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

import fi.metatavu.kuntaapi.server.integrations.tpt.TptConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;
import fi.metatavu.kuntaapi.test.server.integration.ptv.TestPtvConsts;

@SuppressWarnings ("squid:S1192")
public class TptTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of(TptConsts.TIMEZONE);
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS[2]);
    getTptMocker().mockAreaSearch("Testil%C3%A4", "tpt/area-search.json");
    
    startMocks();
    
    waitApiListCount("/organizations", 1);
    
    createTptSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/jobs", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteTptSettings(organizationId);
  }
  
  @Test
  public void findListJob() {
    String organizationId = getOrganizationId(0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs/{jobId}", organizationId, getOrganizationJobId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("Kokki, Testilän ravintolaan"))
      .body("employmentType", is("yli 12 kuukautta"))
      .body("description", is("Etsitään à la carte- kokkia."))
      .body("location", is("Testilä"))
      .body("organisationalUnit", is("Testilän Ravintola"))
      .body("duration", is("yli 12 kuukautta"))
      .body("taskArea", is("Kokki"))
      .body("publicationStart", sameInstant(getInstant(2018, 1, 2, 0, 0, TIMEZONE_ID)))
      .body("publicationEnd",  nullValue())
      .body("link", is(getLink(1234567)));
  }
    
  @Test
  public void testListJobs() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("title[1]", is("Lähihoitaja, Testilän sairaalaan"))
      .body("employmentType[1]", is("yli 12 kuukautta"))
      .body("description[1]", is("Hoitaja testilän sairaalaan"))
      .body("location[1]", is("Testilä"))
      .body("organisationalUnit[1]", is("Testilän sairaala"))
      .body("duration[1]", is("yli 12 kuukautta, sijaisuuksia"))
      .body("taskArea[1]", is("Lähihoitaja"))
      .body("publicationStart[1]", sameInstant(getInstant(2018, 2, 2, 0, 0, TIMEZONE_ID)))
      .body("publicationEnd[1]",  sameInstant(getInstant(2018, 4, 14, 0, 0, TIMEZONE_ID)))
      .body("link[1]", is(getLink(2345678)));
  } 
  
  @Test
  public void testListJobsSort() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_END&sortDir=ASCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("link[2]", is(getLink(3456789)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_END&sortDir=DESCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("link[0]", is(getLink(3456789)));
      
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_START&sortDir=ASCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[2]", notNullValue())
      .body("link[2]", is(getLink(3456789)));

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs?sortBy=PUBLICATION_START&sortDir=DESCENDING", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("link[0]", is(getLink(3456789)));
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
  
  @Test
  public void testListRemovedJobs() throws InterruptedException {
    getTptMocker().endMock();
    getTptMocker().mockAreaSearch("Testil%C3%A4", "tpt/area-search-removed.json");
    getTptMocker().startMock();
    
    waitApiListCount(String.format("/organizations/%s/jobs", getOrganizationId(0)), 2); 

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/jobs", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("id[0]", notNullValue())
      .body("title[0]", is("Lähihoitaja, Testilän sairaalaan"))
      .body("employmentType[0]", is("yli 12 kuukautta"))
      .body("description[0]", is("Hoitaja testilän sairaalaan"))
      .body("location[0]", is("Testilä"))
      .body("organisationalUnit[0]", is("Testilän sairaala"))
      .body("duration[0]", is("yli 12 kuukautta, sijaisuuksia"))
      .body("taskArea[0]", is("Lähihoitaja"))
      .body("publicationStart[0]", sameInstant(getInstant(2018, 2, 2, 0, 0, TIMEZONE_ID)))
      .body("publicationEnd[0]",  sameInstant(getInstant(2018, 4, 14, 0, 0, TIMEZONE_ID)))
      .body("link[0]", is(getLink(2345678)));
  }

  private void createTptSettings(String organizationId) {
    insertOrganizationSetting(organizationId, TptConsts.ORGANIZATION_SETTING_AREA, "Testilä");
    insertSystemSetting(TptConsts.SYSTEM_SETTING_BASE_URL, getWireMockBasePath());
    flushCache();
  }
   
  private void deleteTptSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, TptConsts.ORGANIZATION_SETTING_AREA);
    deleteSystemSetting(TptConsts.SYSTEM_SETTING_BASE_URL);
  }

  private String getLink(Integer number) {
    return String.format("%s/tpt/%d", getWireMockBasePath(), number);
  }
  
}
