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

    getRestfulPtvServiceMocker()
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba", "ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    getRestfulPtvElectronicServiceChannelMocker()
      .mockElectronicServiceChannels("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a");
    
    getRestfulPtvPhoneServiceChannelMocker()
      .mockPhoneServiceChannels("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d");
      
    getRestfulPtvPrintableFormServiceChannelMocker()
      .mockPrintableFormServiceChannels("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36");
      
    getRestfulPtvServiceLocationServiceChannelMocker()
      .mockServiceLocationServiceChannels("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928");
      
    getRestfulPtvWebPageServiceChannelMocker()
      .mockWebPageServiceChannels("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8");
    
    getRestfulPtvOrganizationMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e", "0f112910-08ca-4942-8c80-476cb710ee1d")
      .mockOrganizationServices("0de268cf-1ea1-4719-8a6e-1150933b6b9e", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+6c9926b9-4aa0-4635-b66a-471af07dfec3", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+822d5347-8398-4866-bb9d-9cdc60b38fba", "0de268cf-1ea1-4719-8a6e-1150933b6b9e+ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    
    getPtvMocker()
      .mockStatutoryDescriptions("2ddfcd49-b0a8-4221-8d8f-4c4d3c5c0ab8")
      .startMock();

    startMocks();

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
