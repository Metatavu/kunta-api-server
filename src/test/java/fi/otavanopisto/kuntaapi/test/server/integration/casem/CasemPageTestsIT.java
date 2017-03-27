package fi.otavanopisto.kuntaapi.test.server.integration.casem;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.integrations.casem.CaseMConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class CasemPageTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createPtvSettings();
    
    getRestfulPtvOrganizationMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e");
    
    getCasemMocker()
      .mockSubnodes(100)
      .mockSubnodes(100, 123)
      .mockSubnodes(100, 123, 234)
      .mockSubnodes(100, 123, 234, 345)
      .mockContentList()
      .mockContent(987, 876, 765)
      .startMock();
    
    getManagementPageMocker()
      .mockPages(456);

    startMocks();

    waitApiListCount("/organizations", 1);
    
    String organizationId = getOrganizationId(0);
    createCasemSettings(organizationId);
    createManagementSettings(organizationId);

    waitApiListCount(String.format("/organizations/%s/pages", getOrganizationId(0)), 5); 
  }

  @After
  public void afterTest() {
    String organizationId = getOrganizationId(0);
    deleteCasemSettings(organizationId);
    deleteManagementSettings(organizationId);
    deletePtvSettings();
    getPtvMocker().endMock();
  }
  
  @Test
  public void testPageRelocate() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    String newParent = getPageIdByPath(organizationId, "/bertha");
    String originalPath = String.format("/organizations/%s/pages?path=/test_board", organizationId);
    String newPath = String.format("/organizations/%s/pages?path=/bertha/test_board", organizationId);
    Page originalPage = getPageByPath(organizationId, "/test_board");
    
    assertPageInPath(originalPath, "test_board", null);
    assertPageNotInPath(newPath);
    
    getManagementPageMappingMocker().addMapping("/test_board", "/bertha");
    
    waitApiListCount(newPath, 1);
    assertPageInPath(newPath, "test_board", newParent);
    assertPageNotInPath(originalPath);

    Page relocatedPage = getPageByPath(organizationId, "/bertha/test_board");
    assertEquals(originalPage.getId(), relocatedPage.getId());
    assertEquals(originalPage.getTitles(), relocatedPage.getTitles());

    getManagementPageMappingMocker().removeMapping("/test_board");
    
    waitApiListCount(originalPath, 1);
    assertPageInPath(originalPath, "test_board", null);
    assertPageNotInPath(newPath);
  }
  
  @Test
  public void testPageRelocateWildcard() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    Page originalPage = getPageByPath(organizationId, "/test_board/meeting_16.1.2017");
    String originalParent = getPageIdByPath(organizationId, "/test_board");
    String newParent = getPageIdByPath(organizationId, "/bertha");
    String originalPath = String.format("/organizations/%s/pages?path=/test_board/meeting_16.1.2017", organizationId);
    String newPath = String.format("/organizations/%s/pages?path=/bertha/meeting_16.1.2017", organizationId);
    
    assertPageInPath(originalPath, "meeting_16.1.2017", originalParent);
    assertPageNotInPath(newPath);

    addServerLogEntry("!!!!!!!!!!!!!! Mappaus päälle.");

    getManagementPageMappingMocker().addMapping("/test_board/*", "/bertha");
    
    waitApiListCount(newPath, 1);
    assertPageInPath(newPath, "meeting_16.1.2017", newParent);
    assertPageNotInPath(originalPath);

    Page relocatedPage = getPageByPath(organizationId, "/bertha/meeting_16.1.2017");
    assertEquals(originalPage.getId(), relocatedPage.getId());
    assertEquals(originalPage.getTitles(), relocatedPage.getTitles());

    getManagementPageMappingMocker().removeMapping("/test_board/*");
    addServerLogEntry("!!!!!!!!!!!!!! Mappaus pois.");
    
    waitApiListCount(originalPath, 1);
    assertPageInPath(originalPath, "meeting_16.1.2017", originalParent);
    assertPageNotInPath(newPath);
  }

  @Test
  public void testFindPage() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages/{pageId}", organizationId, getPageId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("slug", is("test_board"))
      .body("parentId", nullValue())
      .body("titles.size()", is(1))
      .body("titles[0].language", is("fi"))
      .body("titles[0].value", is("Test Board"))
      .body("titles[0].type", nullValue());
  }

  @Test
  public void testListPages() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(4))
      .body("slug[0]", is("test_board"))
      .body("slug[1]", is("meeting_16.1.2017"))
      .body("slug[2]", is("test_1"))
      .body("slug[3]", is("test_2"));
  } 

  @Test
  public void testMetaHideMenuChildren() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(4))
      .body("meta[0].hideMenuChildren", is(true))
      .body("meta[1].hideMenuChildren", is(true))
      .body("meta[2].hideMenuChildren", is(true))
      .body("meta[3].hideMenuChildren", is(true));
  } 
  
  @Test
  public void testListPagesByPath() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/test_board", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("test_board")); 
      
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/test_board/meeting_16.1.2017/test_2", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("test_2")); 
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/test_board/non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testOrganizationPagesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationAnnouncecmentId = getPageId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/pages/%s", organizationId, organizationAnnouncecmentId));
    assertEquals(4, countApiList(String.format("/organizations/%s/pages", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/pages/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/pages/%s", incorrectOrganizationId, organizationAnnouncecmentId));
    assertEquals(0, countApiList(String.format("/organizations/%s/pages", incorrectOrganizationId)));
  }
  
  private void createPtvSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
  
  private void deletePtvSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
    
  private void createCasemSettings(String organizationId) {
    insertOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/casem", getWireMockBasePath(), BASE_URL));
    insertOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE, "100");
    insertOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_DOWNLOAD_PATH, String.format("%s/casemdownload", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteCasemSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_BASEURL);
    deleteOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE);
    deleteOrganizationSetting(organizationId, CaseMConsts.ORGANIZATION_SETTING_DOWNLOAD_PATH);
  }

  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
}
