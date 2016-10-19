package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class OrganizationsServiceTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createSettings();
    getPtvMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e", "0f112910-08ca-4942-8c80-476cb710ee1d")
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba", "ef66b7c2-e938-4a30-ad57-475fc40abf27")
      .mockOrganizationServices("0de268cf-1ea1-4719-8a6e-1150933b6b9e", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+6c9926b9-4aa0-4635-b66a-471af07dfec3", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+822d5347-8398-4866-bb9d-9cdc60b38fba", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+ef66b7c2-e938-4a30-ad57-475fc40abf27")
      .startMock();

    flushCache();
    
    waitApiListCount("/organizations", 2);
    waitApiListCount("/services", 3);
    waitApiListCount(String.format("/organizations/%s/organizationServices", getOrganizationId(0)), 3);
  }

  @After
  public void afterClass() {
    getPtvMocker().endMock();
    deleteSettings();
  }
   
  private void createSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
  }
  
  private void deleteSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
  @Test
  public void testFindOrganizationService() {
    String organizationId = getOrganizationId(0);
    String organizationServiceId = getOrganizationServiceId(organizationId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/organizationServices/{organizationServiceId}", organizationId, organizationServiceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(organizationServiceId))
      .body("serviceId", is(getServiceId(0)))
      .body("organizationId", is(organizationId))
      .body("roleType", is("Responsible"))
      .body("provisionType", nullValue())
      .body("additionalInformation.size()", is(0))
      .body("webPages.size()", is(0));
  } 
  
  @Test
  public void testListOrganizationServices() {
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/organizationServices", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("serviceId[1]", is(getServiceId(1)))
      .body("organizationId[1]", is(organizationId))
      .body("roleType[1]", is("Producer"))
      .body("provisionType[1]", is("SelfProduced"))
      .body("additionalInformation[1].size()", is(0))
      .body("webPages[1].size()", is(0));
  } 
  
  @Test
  public void testListOrganizationServicesLimits() {
    String organizationId = getOrganizationId(0);
    assertListLimits(String.format("/organizations/%s/organizationServices", organizationId), 3);
  }
  
  @Test
  public void testOrganizationServiceNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationServiceId = getOrganizationServiceId(organizationId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/organizationServices/%s", organizationId, organizationServiceId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/organizationServices/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/organizationServices/%s", incorrectOrganizationId, organizationServiceId));
  }
}
