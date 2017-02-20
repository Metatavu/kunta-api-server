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

@SuppressWarnings ("squid:S1192")
public class AgencyTestsIT extends AbstractIntegrationTest{
  
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

    waitApiListCount(String.format("/organizations/%s/transportAgencies", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    getPtvMocker().endMock();
    deletePtvSettings();
    deleteGtfsSettings(organizationId);
  }
  
  @Test
  public void testListAgencies() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportAgencies", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("name[1]", is("Testirinne Oy"))
      .body("url[1]", is("http://www.testirinne.fake"))
      .body("timezone[1]", is("Europe/Helsinki"));
  }
  
  @Test
  public void testFindAgency() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/transportAgencies/{agencyId}", organizationId, getOrganizationAgencyId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("name", is("Testilän Liikenne Oy"))
      .body("url", is("http://www.testilanliikenne.eiole"))
      .body("timezone", is("Europe/Helsinki"));
  }
  
  @Test
  public void testOrganizationAgenciesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationAgencyId = getOrganizationAgencyId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/transportAgencies/%s", organizationId, organizationAgencyId));
    assertEquals(3, countApiList(String.format("/organizations/%s/transportAgencies", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/transportAgencies/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/transportAgencies/%s", incorrectOrganizationId, organizationAgencyId));
    assertEquals(0, countApiList(String.format("/organizations/%s/transportAgencies", incorrectOrganizationId)));
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
