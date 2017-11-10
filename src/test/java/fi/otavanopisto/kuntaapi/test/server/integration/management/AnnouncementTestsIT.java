package fi.otavanopisto.kuntaapi.test.server.integration.management;

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

import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class AnnouncementTestsIT extends AbstractIntegrationTest {

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
    
    getManagementAnnouncementMocker()
      .mockAnnouncements(123, 234, 345);

    startMocks();

    waitApiListCount("/organizations", 1);
    
    createManagementSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/announcements", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testFindAnnouncements() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/announcements/{announcementId}", organizationId, getOrganizationAnnouncementId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("slug", is("test-announcement"))
      .body("title", is("Test announcement"))
      .body("abstract", nullValue())
      .body("published", sameInstant(getInstant(2016, 12, 23, 11, 33, TIMEZONE_ID)));
  } 
  
  @Test
  public void testListAnnouncementsBySlug() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/announcements?slug=aakkostesti", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("aakkostesti"))
      .body("title[0]", is("Ääkköstesti"))
      .body("abstract[0]", nullValue())
      .body("published[0]", sameInstant(getInstant(2016, 12, 23, 11, 33, TIMEZONE_ID)));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/announcements?slug=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testListAnnouncements() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/announcements", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("slug[1]", is("another-test"))
      .body("title[1]", is("Test announcement"))
      .body("abstract[1]", nullValue())
      .body("published[1]", sameInstant(getInstant(2016, 12, 23, 11, 33, TIMEZONE_ID)));
  } 
  
  @Test
  public void testOrganizationAnnouncementsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationAnnouncecmentId = getOrganizationAnnouncementId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/announcements/%s", organizationId, organizationAnnouncecmentId));
    assertEquals(3, countApiList(String.format("/organizations/%s/announcements", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/announcements/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/announcements/%s", incorrectOrganizationId, organizationAnnouncecmentId));
    assertEquals(0, countApiList(String.format("/organizations/%s/announcements", incorrectOrganizationId)));
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
