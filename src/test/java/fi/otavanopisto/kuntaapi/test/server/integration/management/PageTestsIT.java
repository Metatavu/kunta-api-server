package fi.otavanopisto.kuntaapi.test.server.integration.management;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class PageTestsIT extends AbstractIntegrationTest {

  private static final String IMAGE_JPEG = "image/jpeg";

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    getManagementMediaMocker()
      .mockMedias(3001, 3002);
    
    getManagementPageMocker()
      .mockPages(456, 567, 678);

    startMocks();
    
    waitApiListCount("/organizations", 1);
    String organizationId = getOrganizationId(0);
    createManagementSettings(organizationId);

    waitApiListCount(String.format("/organizations/%s/pages", organizationId), 3);
    
    String pageId = getPageId(organizationId, 0);
    waitApiListCount(String.format("/organizations/%s/pages/%s/images/", organizationId, pageId), 1);
  }
  
  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testPageRelocate() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    String newParent = getPageId(organizationId, 0);
    String originalPath = String.format("/organizations/%s/pages?path=/bertha", organizationId);
    String newPath = String.format("/organizations/%s/pages?path=/zeus/bertha", organizationId);
    Page originalPage = getPageByPath(organizationId, "/bertha");
    
    assertPageInPath(originalPath, "bertha", null);
    assertPageNotInPath(newPath);
    
    getManagementPageMappingMocker().addMapping("/bertha", "/zeus");
    
    waitApiListCount(newPath, 1);
    assertPageInPath(newPath, "bertha", newParent);
    assertPageNotInPath(originalPath);

    Page relocatedPage = getPageByPath(organizationId, "/zeus/bertha");
    assertEquals(originalPage.getId(), relocatedPage.getId());
    assertEquals(originalPage.getTitles(), relocatedPage.getTitles());
    
    getManagementPageMappingMocker().removeMapping("/bertha");
    
    waitApiListCount(originalPath, 1);
    assertPageInPath(originalPath, "bertha", null);
    assertPageNotInPath(newPath);
  }
  
  @Test
  public void testPageMove() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    String pageId = getPageId(organizationId, 0);
    String newParentId = getPageId(organizationId, 1);
    String firstParentId = getPageId(organizationId, 2);
    String firstParentPath = String.format("/organizations/%s/pages/?parentId=%s", organizationId, firstParentId);
    String newParentPath = String.format("/organizations/%s/pages/?parentId=%s", organizationId, newParentId);
    
    assertEquals(0, countApiList(firstParentPath));
    
    given()
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages/{pageId}", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("parentId", nullValue());
    
    getManagementPageMocker().mockAlternative(678, "parent_456");
    
    waitApiListCount(firstParentPath, 1);
    
    given()
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages/{pageId}", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("parentId", is(firstParentId));
    
    getManagementPageMocker().mockAlternative(678, "parent_567");
    
    waitApiListCount(firstParentPath, 0);
    waitApiListCount(newParentPath, 1);
    
    given()
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages/{pageId}", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("parentId", is(newParentId));
  }
  
  @Test
  public void testFindPages() {
    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages/{pageId}", organizationId, getPageId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("slug", is("zeus"))
      .body("parentId", nullValue())
      .body("titles.size()", is(1))
      .body("titles[0].language", is("fi"))
      .body("titles[0].value", is("Zeus"))
      .body("titles[0].type", nullValue());
  }
  
  @Test
  public void testListPages() {
    // Zeus shoud be listed before abraham and bertha because it's menu_order is set to -100 

    String organizationId = getOrganizationId(0);
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("slug[0]", is("zeus"))
      .body("slug[1]", is("abraham"))
      .body("slug[2]", is("bertha"));
  }
  
  @Test
  public void testListPagesByPath() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/bertha", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("bertha")); 
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/pages?path=/non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
    
  @Test
  public void testOrganizationPagesNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String pageId = getPageId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/pages/%s", organizationId, pageId));
    assertEquals(3, countApiList(String.format("/organizations/%s/pages", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/pages/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/pages/%s", incorrectOrganizationId, pageId));
    assertEquals(0, countApiList(String.format("/organizations/%s/pages", incorrectOrganizationId)));
  }
  
  @Test
  public void testPageImage() {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 0);
    String imageId = getPageImageId(organizationId, pageId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images/{IMAGEID}", organizationId, pageId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(imageId))
      .body("contentType", is(IMAGE_JPEG));
  }
  
  @Test
  public void testPageMultipleImages() {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 1);
    String featuredImageId = getPageImageId(organizationId, pageId, 0);
    String bannerImageId = getPageImageId(organizationId, pageId, 1);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images/{IMAGEID}", organizationId, pageId, featuredImageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(featuredImageId))
      .body("type", is("featured"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images/{IMAGEID}", organizationId, pageId, bannerImageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(bannerImageId))
      .body("type", is("banner"));
  }
  
  @Test
  public void testPageListImagesByType() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 1);
    
    waitApiListCount(String.format("/organizations/%s/pages/%s/images", organizationId, pageId), 2);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images?type=banner", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("type[0]", is("banner"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images?type=featured", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("type[0]", is("featured"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images?type=invalid", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  @Test
  public void testPageContentImages() throws IOException {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 2);
    String imageId = getPageImageId(organizationId, pageId, 0);
    String pageContent = getPageContent(organizationId, pageId);
    
    Elements pageImages = Jsoup.parse(pageContent).select("img");
    assertEquals(2, pageImages.size());
    
    assertEquals("about:blank", pageImages.get(0).attr("src"));
    assertEquals(organizationId, pageImages.get(0).attr("data-organization-id"));
    assertEquals( pageId, pageImages.get(0).attr("data-page-id"));
    assertEquals(imageId, pageImages.get(0).attr("data-attachment-id"));
    assertEquals(ManagementConsts.ATTACHMENT_TYPE_PAGE_CONTENT_IMAGE, pageImages.get(0).attr("data-image-type"));
    assertEquals("http://example.com/image.jpg", pageImages.get(1).attr("src"));
      
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images", organizationId, pageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("contentType[0]", is(IMAGE_JPEG))
      .body("type[0]", is("content-image"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images/{IMAGEID}", organizationId, pageId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("contentType", is(IMAGE_JPEG))
      .body("type", is("content-image"));
  }

  @Test
  public void testPageImageNotFound() {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 0);
    String incorrectPageId = getPageId(organizationId, 2);
    String imageId = getPageImageId(organizationId, pageId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/pages/%s/images/%s", organizationId, pageId, imageId));
    assertEquals(1, countApiList(String.format("/organizations/%s/pages/%s/images", organizationId, pageId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/pages/%s/images/%s", organizationId, pageId, malformedId));
    }

    assertNotFound(String.format("/organizations/%s/pages/%s/images/%s", organizationId, incorrectPageId, imageId));
  }

  @Test
  public void testPageImageData() {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 0);
    String imageId = getPageImageId(organizationId, pageId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{PAGEID}/images/{IMAGEID}/data", organizationId, pageId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "21621")
      .header("Content-Type", IMAGE_JPEG);
  }
  
  @Test
  public void testPageImageDataScaled() {
    String organizationId = getOrganizationId(0);
    String pageId = getPageId(organizationId, 0);
    String imageId = getPageImageId(organizationId, pageId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/pages/{EVENTID}/images/{IMAGEID}/data?size=100", organizationId, pageId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "2033")
      .header("Content-Type", IMAGE_JPEG);
  }
  
  @Test
  public void testPageUnarchive() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    String pageId = getPageId(organizationId, 2);
    assertNotNull(pageId);
    
    getManagementPageMocker().unmockPages(456);
    waitApiListCount(String.format("/organizations/%s/pages", organizationId), 2);
    
    assertNull(getPageId(organizationId, 2));
    
    getManagementPageMocker().mockPages(456);
    waitApiListCount(String.format("/organizations/%s/pages", organizationId), 3);

    assertEquals(pageId, getPageId(organizationId, 2));
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
