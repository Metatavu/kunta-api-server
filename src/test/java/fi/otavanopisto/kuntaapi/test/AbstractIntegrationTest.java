package fi.otavanopisto.kuntaapi.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.specification.RequestSpecification;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;

/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  public static final String BASE_URL = "/v1";
  
  private KuntarekryMocker kuntarekryMocker = new KuntarekryMocker();
  
  private ManagementPageMappingMocker managementPageMappingMocker = new ManagementPageMappingMocker();
  private ManagementTagMocker managementTagMocker = new ManagementTagMocker();
  private ManagementCategoryMocker managementCategoryMocker = new ManagementCategoryMocker();
  private ManagementPageMocker managementPageMocker = new ManagementPageMocker();
  private ManagementPostMocker managementPostMocker = new ManagementPostMocker();
  private ManagementShortlinkMocker managementShortlinkMocker = new ManagementShortlinkMocker();
  private ManagementIncidentMocker managementIncidentMocker = new ManagementIncidentMocker();
  private ManagementAnnouncementMocker managementAnnouncementMocker = new ManagementAnnouncementMocker();
  private ManagementBannerMocker managementBannerMocker = new ManagementBannerMocker();
  private ManagementFragmentMocker managementFragmentMocker = new ManagementFragmentMocker();
  private ManagementMediaMocker managementMediaMocker = new ManagementMediaMocker();
  private ManagementTileMocker managementTileMocker = new ManagementTileMocker();
  private ManagementMenuMocker managementMenuMocker = new ManagementMenuMocker();
  private LinkedEventsEventMocker linkedEventsEventMocker = new LinkedEventsEventMocker();
  private PtvServiceMocker ptvServiceMocker = new PtvServiceMocker();
  private PtvServiceChannelMocker ptvServiceChannelMocker = new PtvServiceChannelMocker();
  private PtvOrganizationMocker ptvOrganizationMocker = new PtvOrganizationMocker();
  private PtvCodesMocker ptvCodesMocker = new PtvCodesMocker();
  
  @After
  public void afterEveryTest() throws IOException {
    setLog4jLevel(Level.OFF);
    
    addServerLogEntry(String.format("### Test %s end ###", testName.getMethodName()));
    
    deleteSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING);
    clearTasks();
    
    managementMenuMocker.endMock();
    managementPageMappingMocker.endMock();
    managementPageMocker.endMock();
    managementPostMocker.endMock();
    managementShortlinkMocker.endMock();
    managementIncidentMocker.endMock();
    managementAnnouncementMocker.endMock();
    managementBannerMocker.endMock();
    managementFragmentMocker.endMock();
    managementMediaMocker.endMock();
    managementTileMocker.endMock();
    managementCategoryMocker.endMock();
    managementTagMocker.endMock();
    linkedEventsEventMocker.endMock();
    ptvServiceMocker.stop();
    ptvServiceChannelMocker.stop();
    ptvOrganizationMocker.stop();
    ptvCodesMocker.endMock();

    if (dropIdentifiersAfter()) {
      deleteIdentifiers();
    }
    
    deleteSystemSettings();
  }
  
  private void deleteIndices() {
    try {
      String types = "service";
      String body = "{\"query\": {\"match_all\": {} } }";
      HttpPost httpPost = new HttpPost(String.format("http://localhost:9200/kunta-api/%s/_delete_by_query?conflicts=proceed", types));
      httpPost.setEntity(new StringEntity(body));
      
      CloseableHttpClient httpClient = HttpClients.createDefault();
      try {
        httpClient.execute(httpPost);
      } finally {
        httpClient.close();
      }
    } catch (IOException e) {
      
    }
  }

  protected  boolean dropIdentifiersAfter() {
    return true;
  }

  public void startMocks() {
    if (dropIdentifiersAfter()) {
      waitForElasticIndex();
      deleteIndices();
    }
    
    createSystemSettings();
    
    ptvOrganizationMocker.start();
    ptvServiceChannelMocker.start();
    ptvServiceMocker.start();
    linkedEventsEventMocker.startMock();
    kuntarekryMocker.startMock();
    managementPageMocker.startMock();
    managementPostMocker.startMock();
    managementShortlinkMocker.startMock();
    managementIncidentMocker.startMock();
    managementAnnouncementMocker.startMock();
    managementBannerMocker.startMock();
    managementFragmentMocker.startMock();
    managementMediaMocker.startMock();
    managementTileMocker.startMock();
    managementPageMappingMocker.startMock();
    managementCategoryMocker.startMock();
    managementTagMocker.startMock();
    managementMenuMocker.startMock();
    ptvCodesMocker.startMock();
    
    insertSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING, "true");
    
    addServerLogEntry(String.format("### Test %s start ###", testName.getMethodName()));
    setLog4jLevel(Level.WARN);
  }
  
  public PtvServiceMocker getPtvServiceMocker() {
    return ptvServiceMocker;
  }

  public KuntarekryMocker getKuntarekryMocker() {
    return kuntarekryMocker;
  }
  
  public ManagementMenuMocker getManagementMenuMocker() {
    return managementMenuMocker;
  }

  public ManagementPageMappingMocker getManagementPageMappingMocker() {
    return managementPageMappingMocker;
  }
  
  public ManagementPageMocker getManagementPageMocker() {
    return managementPageMocker;
  }
  
  public ManagementPostMocker getManagementPostMocker() {
    return managementPostMocker;
  }
  
  public ManagementShortlinkMocker getManagementShortlinkMocker() {
    return managementShortlinkMocker;
  }

  public ManagementIncidentMocker getManagementIncidentMocker() {
    return managementIncidentMocker;
  }
  
  public ManagementAnnouncementMocker getManagementAnnouncementMocker() {
    return managementAnnouncementMocker;
  }
  
  public ManagementMediaMocker getManagementMediaMocker() {
    return managementMediaMocker;
  }
  
  public ManagementBannerMocker getManagementBannerMocker() {
    return managementBannerMocker;
  }
  
  public ManagementFragmentMocker getManagementFragmentMocker() {
    return managementFragmentMocker;
  }
  
  public ManagementTileMocker getManagementTileMocker() {
    return managementTileMocker;
  }
  
  public ManagementCategoryMocker getManagementCategoryMocker() {
    return managementCategoryMocker;
  }
  
  public ManagementTagMocker getManagementTagMocker() {
    return managementTagMocker;
  }
  
  public LinkedEventsEventMocker getLinkedEventsEventMocker() {
    return linkedEventsEventMocker;
  }
  
  public PtvServiceChannelMocker getPtvServiceChannelMocker() {
    return ptvServiceChannelMocker;
  }
  
  public PtvOrganizationMocker getPtvOrganizationMocker() {
    return ptvOrganizationMocker;
  }
  
  public PtvCodesMocker getPtvCodesMocker() {
    return ptvCodesMocker;
  }

  /**
   * Returns test client id for given access type
   * 
   * @param accessType access type
   * @return clientId
   */
  protected String getClientId(AccessType accessType) {
    return String.format("%s_ID", accessType.name());
  }


  /**
   * Returns test client secret for given access type
   * 
   * @param accessType access type
   * @return clientSecret
   */
  protected String getClientSecret(AccessType accessType) {
    return String.format("%s_SECRET", accessType.name());
  }
  
  /**
   * Adds a log entry into the server log
   * 
   * @param text log entry text
   */
  protected void addServerLogEntry(String text) {
    givenUnrestricted()
      .get(String.format("/system/log?text=%s", text))
      .then()
      .statusCode(200);
  }
  
  /**
   * Returns REST assurred request specification autheticated with test client with unrestricted access to API
   * 
   * @return REST assurred request specification autheticated with test client with unrestricted access to API
   */
  protected RequestSpecification givenUnrestricted() {
    return givenAuthenticated(AccessType.UNRESTRICTED);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read only access to API
   * 
   * @return REST assurred request specification autheticated with test client with read only access to API
   */
  protected RequestSpecification givenReadonly() {
    return givenAuthenticated(AccessType.READ_ONLY);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read write access to API
   * 
   * @return REST assurred request specification autheticated with test client with read write access to API
   */
  protected RequestSpecification givenReadWrite() {
    return givenAuthenticated(AccessType.READ_WRITE);
  }
  
  /**
   * Returns REST assurred request specification autheticated with test client of given access type
   * 
   * @param accessType access type
   * @return REST assurred request specification autheticated with test client of given access type
   */
  protected RequestSpecification givenAuthenticated(AccessType accessType) {
    return given()
      .baseUri(getApiBasePath())
      .auth().preemptive().basic(getClientId(accessType), getClientSecret(accessType));
  }

  protected void flushCache() {
    givenUnrestricted()
      .get("/system/jpa/cache/flush")
      .then();
  }
  
  protected void clearTasks() {
    executeDelete("delete from Task");
  }
  
  @SuppressWarnings ({"squid:S1166", "squid:S00108", "squid:S2925", "squid:S106"})
  protected void waitApiListCount(String path, int count) throws InterruptedException {
    int counter = 0;
    long timeout = System.currentTimeMillis() + (60 * 1000 * 5);
    while (true) {
      counter++;
      Thread.sleep(1000);
      try {
        int listCount = countApiList(path);
        if (listCount == count) {
          return;
        }
        
        if (System.currentTimeMillis() > timeout) {
          fail(String.format("Timeout waiting for %s to have count %d", path, count));
        }
        
        if ((counter % 10) == 0) {
          System.out.println(String.format("... still waiting %d items in %s, current count %d", count, path, listCount));
        }
        
      } catch (JsonPathException e) {
        
      }
    }
  }

  protected void assertFound(String url) {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(url)
      .then()
      .assertThat()
      .statusCode(200);
  }

  protected void assertNotFound(String url) {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(url)
      .then()
      .assertThat()
      .statusCode(404);
  }
  
  protected String getOrganizationAgencyId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportAgencies", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationScheduleId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportSchedules", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationRouteId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportRoutes", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationStopId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportStops", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationStopTimeId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportStopTimes", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationTripId(String organizationId, int index) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/%s/transportTrips", organizationId))
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationServiceId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/organizationServices", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationJobId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/jobs", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationAnnouncementId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/announcements", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationEventId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/events", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationFragmentId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/fragments", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationShortlinkId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/shortlinks", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationIncidentId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/incidents", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationEmergencyId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/emergencies", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getBannerId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/banners", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getBannerImageId(String organizationId, String bannerId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/banners/{bannerId}/images", organizationId, bannerId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getTileId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/tiles", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getTileImageId(String organizationId, String tileId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/tiles/{tileId}/images", organizationId, tileId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getPageId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }

  protected String getPageIdByPath(String organizationId, String path) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages?path=%s", organizationId, path))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", 0));
  }

  protected Page getPageByPath(String organizationId, String path) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages?path=%s", organizationId, path))
        .body()
        .jsonPath()
        .getObject("[0]", Page.class);
  }
  
  protected String getPageImageId(String organizationId, String pageId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/pages/{pageId}/images", organizationId, pageId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getPageContent(String organizationId, String pageId) throws IOException {
    LocalizedValue[] pageContents = getPageContents(organizationId, pageId);
    if (pageContents.length > 0) {
      return pageContents[0].getValue();
    }
    
    return null;
  }
  
  protected LocalizedValue[] getPageContents(String organizationId, String pageId) throws IOException {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/pages/{pageId}/content", organizationId, pageId)
        .body()
        .as(LocalizedValue[].class);
  }
  
  protected String getNewsArticleId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/news", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getNewsArticleImageId(String organizationId, String newsArticleId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/news/{newsArticleId}/images", organizationId, newsArticleId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationId(int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations")
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getServiceId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/services", waitCount);
    
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/services")
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getEventId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/events", organizationId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getEventImageId(String organizationId, String eventId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/events/{eventId}/images", organizationId, eventId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getMenuId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/menus", organizationId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }

  protected String getContactId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/contacts", organizationId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getMenuItemId(String organizationId, String menuId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/menus/{menuId}/items", organizationId, menuId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getElectronicChannelId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/electronicServiceChannels", waitCount);
    
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getElectronicChannelId(int index) throws InterruptedException {
    return getElectronicChannelId(index, 3);
  }
  
  protected String getPhoneChannelId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/phoneServiceChannels", waitCount);
    
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getPhoneChannelId(int index) throws InterruptedException {
    return getPhoneChannelId(index, 3);
  }
  
  protected String getPrintableFormChannelId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/printableFormServiceChannels", waitCount);
    
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getPrintableFormChannelId(int index) throws InterruptedException {
    return getPrintableFormChannelId(index, 3);
  }
  
  protected String getServiceLocationChannelId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/serviceLocationServiceChannels", waitCount);
    
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getServiceLocationChannelId(int index) throws InterruptedException {
    return getServiceLocationChannelId(index, 3); 
  }
  
  protected String getWebPageChannelId(int index, int waitCount) throws InterruptedException {
    waitApiListCount("/webPageServiceChannels", waitCount);
    
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getWebPageChannelId(int index) throws InterruptedException {
    return getWebPageChannelId(index, 3);
  }
  
  protected int countApiList(String path) {
    return givenReadonly()
      .contentType(ContentType.JSON)
      .get(path)
      .andReturn()
      .body()
      .jsonPath()
      .get("size()");
  }
  
  protected void assertListLimits(String basePath, int maxResults) {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=1", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(maxResults - 1));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=2", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(Math.max(maxResults - 2, 0)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=666", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=-1", basePath))
      .then()
      .assertThat()
      .statusCode(400);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?maxResults=2", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(Math.min(maxResults, 2)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?maxResults=0", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?maxResults=-1", basePath))
      .then()
      .assertThat()
      .statusCode(400);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?maxResults=666", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(maxResults));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=0&maxResults=2", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(Math.min(maxResults, 2)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=1&maxResults=2", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(Math.min(maxResults - 1, 2)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=1&maxResults=1", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(Math.min(maxResults - 1, 1)));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=-1&maxResults=1", basePath))
      .then()
      .assertThat()
      .statusCode(400);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=2&maxResults=-1", basePath))
      .then()
      .assertThat()
      .statusCode(400);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=1&maxResults=0", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("%s?firstResult=21&maxResults=20", basePath))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  protected void assertPageInPath(String path, String expectedSlug, String expectedParentId) {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(path)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("slug[0]", is(expectedSlug))
      .body("parentId[0]", is(expectedParentId)); 
  }
  
  protected void assertPageNotInPath(String path) {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(path)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  protected boolean inTravis() {
    return "true".equalsIgnoreCase(System.getenv("TRAVIS"));
  }

  protected boolean skipElasticSearchTests() {
    return "true".equalsIgnoreCase(System.getProperty("it.skipelasticsearch"));
  }
  
  protected void waitForElasticIndex() {
    waitMs(3000);    
  }
  
  private void setLog4jLevel(Level level) {
    LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    Configuration config = loggerContext.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
    loggerConfig.setLevel(level);
    loggerContext.updateLoggers();
  }
  
  protected void waitMs(int ms) {
    if (ms > 0) {
      try {
        Thread.sleep(ms);
      } catch (InterruptedException e) {
      }
    }
  }
  
  private void createSystemSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), "/ptv"));
  }
  
  private void deleteSystemSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
}