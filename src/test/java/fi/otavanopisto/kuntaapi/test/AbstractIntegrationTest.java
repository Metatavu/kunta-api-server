package fi.otavanopisto.kuntaapi.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.exception.JsonPathException;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  public static final String BASE_URL = "/v1";
  
  private RestFulPtvMocker ptvMocker = new RestFulPtvMocker();
  private KuntarekryMocker kuntarekryMocker = new KuntarekryMocker();
  private ManagementMocker managementMocker = new ManagementMocker();
  private CasemMocker casemMocker = new CasemMocker();
  private ManagementPageMocker managementPageMocker = new ManagementPageMocker();
  private ManagementPostMocker managementPostMocker = new ManagementPostMocker();
  private ManagementShortlinkMocker managementShortlinkMocker = new ManagementShortlinkMocker();
  private ManagementAnnouncementMocker managementAnnouncementMocker = new ManagementAnnouncementMocker();
  private ManagementBannerMocker managementBannerMocker = new ManagementBannerMocker();
  private ManagementFragmentMocker managementFragmentMocker = new ManagementFragmentMocker();
  private ManagementMediaMocker managementMediaMocker = new ManagementMediaMocker();
  private ManagementTileMocker managementTileMocker = new ManagementTileMocker();
  private RestfulPtvServiceMocker restfulPtvServiceMocker = new RestfulPtvServiceMocker();
  private RestfulPtvOrganizationMocker restfulPtvOrganizationMocker = new RestfulPtvOrganizationMocker();
  private RestfulPtvElectronicServiceChannelMocker restfulPtvElectronicServiceChannelMocker = new RestfulPtvElectronicServiceChannelMocker();
  private RestfulPtvPhoneServiceChannelMocker restfulPtvPhoneServiceChannelMocker = new RestfulPtvPhoneServiceChannelMocker();
  private RestfulPtvPrintableFormServiceChannelMocker restfulPtvPrintableFormServiceChannelMocker = new RestfulPtvPrintableFormServiceChannelMocker();
  private RestfulPtvServiceLocationServiceChannelMocker restfulPtvServiceLocationServiceChannelMocker = new RestfulPtvServiceLocationServiceChannelMocker();
  private RestfulPtvWebPageServiceChannelMocker restfulPtvWebPageServiceChannelMocker = new RestfulPtvWebPageServiceChannelMocker();
  
  
  @After
  public void afterEveryTest() {
    setLog4jLevel(Level.OFF);
    
    addServerLogEntry(String.format("### Test %s end ###", testName.getMethodName()));
    
    deleteSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING);
    clearTasks();
    
    managementPageMocker.endMock();
    managementPostMocker.endMock();
    managementShortlinkMocker.endMock();
    managementAnnouncementMocker.endMock();
    managementBannerMocker.endMock();
    managementFragmentMocker.endMock();
    managementMediaMocker.endMock();
    managementTileMocker.endMock();
    
    restfulPtvServiceMocker.endMock();
    restfulPtvOrganizationMocker.endMock();
    restfulPtvElectronicServiceChannelMocker.endMock();
    restfulPtvPhoneServiceChannelMocker.endMock();
    restfulPtvPrintableFormServiceChannelMocker.endMock();
    restfulPtvServiceLocationServiceChannelMocker.endMock();
    restfulPtvWebPageServiceChannelMocker.endMock();
    
    deleteIdentifiers();    
  }
  
  public void startMocks() {
    restfulPtvElectronicServiceChannelMocker.startMock();
    restfulPtvPhoneServiceChannelMocker.startMock();
    restfulPtvPrintableFormServiceChannelMocker.startMock();
    restfulPtvServiceLocationServiceChannelMocker.startMock();
    restfulPtvWebPageServiceChannelMocker.startMock();
    restfulPtvOrganizationMocker.startMock();
    restfulPtvServiceMocker.startMock();
    kuntarekryMocker.startMock();
    managementMocker.startMock();
    casemMocker.startMock();
    managementPageMocker.startMock();
    managementPostMocker.startMock();
    managementShortlinkMocker.startMock();
    managementAnnouncementMocker.startMock();
    managementBannerMocker.startMock();
    managementFragmentMocker.startMock();
    managementMediaMocker.startMock();
    managementTileMocker.startMock();

    insertSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING, "true");
    
    addServerLogEntry(String.format("### Test %s start ###", testName.getMethodName()));
    setLog4jLevel(Level.WARN);
  }
  
  public RestFulPtvMocker getPtvMocker() {
    return ptvMocker;
  }

  public KuntarekryMocker getKuntarekryMocker() {
    return kuntarekryMocker;
  }
  
  public ManagementMocker getManagementMocker() {
    return managementMocker;
  }
  
  public CasemMocker getCasemMocker() {
    return casemMocker;
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
  
  public RestfulPtvOrganizationMocker getRestfulPtvOrganizationMocker() {
    return restfulPtvOrganizationMocker;
  }
  
  public RestfulPtvServiceMocker getRestfulPtvServiceMocker() {
    return restfulPtvServiceMocker;
  }
  
  public RestfulPtvElectronicServiceChannelMocker getRestfulPtvElectronicServiceChannelMocker() {
    return restfulPtvElectronicServiceChannelMocker;
  }
  
  public RestfulPtvPhoneServiceChannelMocker getRestfulPtvPhoneServiceChannelMocker() {
    return restfulPtvPhoneServiceChannelMocker;
  }
  
  public RestfulPtvPrintableFormServiceChannelMocker getRestfulPtvPrintableFormServiceChannelMocker() {
    return restfulPtvPrintableFormServiceChannelMocker;
  }
  
  public RestfulPtvServiceLocationServiceChannelMocker getRestfulPtvServiceLocationServiceChannelMocker() {
    return restfulPtvServiceLocationServiceChannelMocker;
  }
  
  public RestfulPtvWebPageServiceChannelMocker getRestfulPtvWebPageServiceChannelMocker() {
    return restfulPtvWebPageServiceChannelMocker;
  }

  protected void addServerLogEntry(String text) {
    given()
      .baseUri(getApiBasePath())
      .get(String.format("/system/log?text=%s", text))
      .then()
      .statusCode(200);
  }

  protected void flushCache() {
    given()
      .baseUri(getApiBasePath())
      .get("/system/jpa/cache/flush")
      .then()
      .statusCode(200);
  }
  
  protected void clearTasks() {
    executeDelete("delete from Task");
  }

  @SuppressWarnings ({"squid:S1166", "squid:S00108", "squid:S2925", "squid:S106"})
  protected void waitApiListCount(String path, int count) throws InterruptedException {
    int counter = 0;
    long timeout = System.currentTimeMillis() + (120 * 1000);
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
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(url)
      .then()
      .assertThat()
      .statusCode(200);
  }

  protected void assertNotFound(String url) {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(url)
      .then()
      .assertThat()
      .statusCode(404);
  }
  
  protected String getOrganizationAgencyId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportAgencies", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationScheduleId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportSchedules", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationRouteId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportRoutes", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationStopId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportStops", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationStopTimeId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportStopTimes", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationTripId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/transportTrips", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationServiceId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/organizationServices", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationJobId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/jobs", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationAnnouncementId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/announcements", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationFragmentId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/fragments", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationShortlinkId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/shortlinks", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getBannerId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/banners", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getBannerImageId(String organizatinoId, String bannerId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/banners/{bannerId}/images", organizatinoId, bannerId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getTileId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/tiles", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getTileImageId(String organizatinoId, String tileId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/tiles/{tileId}/images", organizatinoId, tileId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getPageId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getPageImageId(String organizatinoId, String pageId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/pages/{pageId}/images", organizatinoId, pageId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getNewsArticleId(String organizationId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/news", organizationId))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getNewsArticleImageId(String organizatinoId, String newsArticleId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/news/{newsArticleId}/images", organizatinoId, newsArticleId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getOrganizationId(int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations")
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getServiceId(int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/services")
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getEventId(String organizatinoId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/events", organizatinoId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getEventImageId(String organizatinoId, String eventId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/events/{eventId}/images", organizatinoId, eventId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getMenuId(String organizatinoId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/menus", organizatinoId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected String getMenuItemId(String organizatinoId, String menuId, int index) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/menus/{menuId}/items", organizatinoId, menuId)
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", index));
  }
  
  protected int countApiList(String path) {
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(path)
      .andReturn()
      .body()
      .jsonPath()
      .get("size()");
  }
  
  protected void assertListLimits(String basePath, int maxResults) {
    given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=1", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(maxResults - 1));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=2", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(maxResults - 2));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=666", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(0));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=-1", basePath))
    .then()
    .assertThat()
    .statusCode(400);
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?maxResults=2", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(2));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?maxResults=0", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(0));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?maxResults=-1", basePath))
    .then()
    .assertThat()
    .statusCode(400);
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?maxResults=666", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(maxResults));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=0&maxResults=2", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(2));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=1&maxResults=2", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(2));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=1&maxResults=1", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(1));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=-1&maxResults=1", basePath))
    .then()
    .assertThat()
    .statusCode(400);
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=2&maxResults=-1", basePath))
    .then()
    .assertThat()
    .statusCode(400);
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=1&maxResults=0", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(0));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=21&maxResults=20", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(0));
  }
  
  private void setLog4jLevel(Level level) {
    LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    Configuration config = loggerContext.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
    loggerConfig.setLevel(level);
    loggerContext.updateLoggers();
  }
  
}