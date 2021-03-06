package fi.metatavu.kuntaapi.test.server.integration.management;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;
import fi.metatavu.kuntaapi.test.server.integration.ptv.TestPtvConsts;

@SuppressWarnings ("squid:S1192")
public class PostTestsIT extends AbstractIntegrationTest {

  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  private static final String IMAGE_JPEG = "image/jpeg";
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock(TestPtvConsts.ORGANIZATIONS[2]);
    
    getManagementTagMocker()
      .mockTags(9002);
    
    getManagementCategoryMocker()
      .mockCategories(9001, 9003);
    
    getManagementPostMocker()
      .mockPosts(789, 890, 901);
    
    getManagementPostMenuOrderMocker()
      .mockMenuOrders(789, 890, 901);
    
    getManagementMediaMocker()
      .mockMedias(3001, 3002);
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/news", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }

  @Test
  public void testFindPosts() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news/{newsArticleId}", organizationId, getNewsArticleId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("title", is("Lorem ipsum dolor sit amet"))
      .body("slug", is("lorem-ipsum-dolor-sit-amet"))
      .body("abstract", containsString("Consectetur adipiscing elit"))
      .body("contents", containsString("Aenean a pellentesque erat"))
      .body("published", sameInstant(getInstant(2017, 01, 12, 11, 38, TIMEZONE_ID)));
  }
  
  @Test
  public void testMoreLink() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news/{newsArticleId}", organizationId, getNewsArticleId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("abstract", containsString("Consectetur adipiscing elit"))
      .body("abstract", not(containsString("more-link")));
  }
  
  @Test
  public void testListPosts() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("slug[0]", is("lorem-ipsum-dolor-sit-amet"))
      .body("slug[1]", is("test-2"))
      .body("slug[2]", is("test-3"));
  } 
  
  @Test
  public void testListPostsPublishedBefore() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?publishedBefore=%s", getIsoDateTime(2017, 1, 12, 14, 00, TIMEZONE_ID)), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("slug[0]", is("lorem-ipsum-dolor-sit-amet"))
      .body("slug[1]", is("test-2"));
  } 
  
  @Test
  public void testListPostsPublishedAfter() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?publishedAfter=%s", getIsoDateTime(2017, 1, 12, 14, 00, TIMEZONE_ID)), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("slug[0]", is("test-3"));
  }
  
  @Test
  public void testListPostsPublishedBetween() {
    String organizationId = getOrganizationId(0);
    String after = getIsoDateTime(2017, 1, 12, 12, 00, TIMEZONE_ID);
    String before = getIsoDateTime(2017, 1, 12, 14, 00, TIMEZONE_ID);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?publishedBefore=%s&publishedAfter=%s", before, after), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("slug[0]", is("test-2"));
  }
  
  @Test
  public void testListPostsOrderLow() throws InterruptedException {
    getManagementPostMenuOrderMocker().mockMenuOrder(789, 20);
    String organizationId = getOrganizationId(0);
    waitNewsArticleSlug(organizationId, 0, "ORDER_NUMBER_PUBLISHED", "test-3");

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?sortBy=ORDER_NUMBER_PUBLISHED", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("slug[0]", is("test-3"))
      .body("slug[1]", is("test-2"))
      .body("slug[2]", is("lorem-ipsum-dolor-sit-amet"));
  } 
  
  @Test
  public void testListPostsOrderHigh() throws InterruptedException {
    getManagementPostMenuOrderMocker().mockMenuOrder(890, -20);
    String organizationId = getOrganizationId(0);
    waitNewsArticleSlug(organizationId, 0, "ORDER_NUMBER_PUBLISHED", "test-2");

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?sortBy=ORDER_NUMBER_PUBLISHED", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("slug[0]", is("test-2"))
      .body("slug[1]", is("test-3"))
      .body("slug[2]", is("lorem-ipsum-dolor-sit-amet"));
  } 
  
  @Test
  public void testListPostsByPath() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?slug=test-3", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("test-3")); 
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?slug=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testListPostsByTag() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    waitApiListCount(String.format("/organizations/%s/news?tag=Yleinen", organizationId), 2);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?tag=Yleinen", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("lorem-ipsum-dolor-sit-amet"))
      .body("tags[0].size()", is(2))
      .body("tags[0][0]", is("Yleinen"))
      .body("tags[0][1]", is("Precious")); 
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?tag=Precious", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("slug[0]", is("lorem-ipsum-dolor-sit-amet"))
      .body("slug[1]", is("test-2"))
      .body("slug[2]", is("test-3"))
      .body("tags[2].size()", is(3))
      .body("tags[2][0]", is("Yleinen"))
      .body("tags[2][1]", is("Test"))
      .body("tags[2][2]", is("Precious")); 
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/news?tag=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testListPostsSearch() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    String organizationId = getOrganizationId(0);
    String search = "(Test page*)|(\"Test page 3\")";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?search=%s", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("slug[0]", is("test-2"))
      .body("slug[1]", is("test-3"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?search=%s&sortBy=SCORE&sortDir=DESC", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("slug[0]", is("test-3"))
      .body("slug[1]", is("test-2"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/news?search=%s&sortBy=SCORE&sortDir=ASC", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("slug[0]", is("test-2"))
      .body("slug[1]", is("test-3"));
  }
    
  @Test
  public void testOrganizationPostsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationNewsArticleId = getNewsArticleId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/news/%s", organizationId, organizationNewsArticleId));
    assertEquals(3, countApiList(String.format("/organizations/%s/news", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/news/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/news/%s", incorrectOrganizationId, organizationNewsArticleId));
    assertEquals(0, countApiList(String.format("/organizations/%s/news", incorrectOrganizationId)));
  }
  
  @Test
  public void testNewsArticleImage() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{NEWSARTICLEID}/images/{IMAGEID}", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(imageId))
      .body("contentType", is(IMAGE_JPEG));
  }
  
  @Test
  public void testNewsArticleImageNotFound() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String incorrectNewsArticleId = getNewsArticleId(organizationId, 2);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, newsArticleId, imageId));
    assertEquals(1, countApiList(String.format("/organizations/%s/news/%s/images", organizationId, newsArticleId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, newsArticleId, malformedId));
    }

    assertNotFound(String.format("/organizations/%s/news/%s/images/%s", organizationId, incorrectNewsArticleId, imageId));
    assertEquals(0, countApiList(String.format("/organizations/%s/news/%s/images", organizationId, incorrectNewsArticleId)));
  }

  @Test
  public void testNewsArticleImageData() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{NEWSARTICLEID}/images/{IMAGEID}/data", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "21621")
      .header("Content-Type", IMAGE_JPEG);
  }
  
  @Test
  public void testNewsArticleImageDataScaled() {
    String organizationId = getOrganizationId(0);
    String newsArticleId = getNewsArticleId(organizationId, 0);
    String imageId = getNewsArticleImageId(organizationId, newsArticleId, 0);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/news/{EVENTID}/images/{IMAGEID}/data?size=100", organizationId, newsArticleId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Length", "3295")
      .header("Content-Type", IMAGE_JPEG);
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
