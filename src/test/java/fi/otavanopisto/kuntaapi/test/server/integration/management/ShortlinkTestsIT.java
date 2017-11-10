package fi.otavanopisto.kuntaapi.test.server.integration.management;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ShortlinkTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    getManagementShortlinkMocker()
      .mockShortlinks(7001, 7002, 7003);
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/shortlinks", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testFindShortlinks() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/shortlinks/{shortlinkId}", organizationId, getOrganizationShortlinkId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("path", is("/elsewhere"))
      .body("name", is("Elsewhere"));
  } 
  
  @Test
  public void testListShortlinksBySlug() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/shortlinks?path=/hills", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("path[0]", is("/hills"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/shortlinks?path=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testListShortlinks() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/shortlinks", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("path[1]", is("/beyond"))
      .body("name[1]", is("Beyond"));
  } 
  
  @Test
  public void testOrganizationShortlinksNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationShortlinkId = getOrganizationShortlinkId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/shortlinks/%s", organizationId, organizationShortlinkId));
    assertEquals(3, countApiList(String.format("/organizations/%s/shortlinks", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/shortlinks/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/shortlinks/%s", incorrectOrganizationId, organizationShortlinkId));
    assertEquals(0, countApiList(String.format("/organizations/%s/shortlinks", incorrectOrganizationId)));
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
