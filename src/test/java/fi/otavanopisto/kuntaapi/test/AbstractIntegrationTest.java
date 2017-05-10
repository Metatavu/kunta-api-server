package fi.otavanopisto.kuntaapi.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.exception.JsonPathException;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

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
  private CasemMocker casemMocker = new CasemMocker();
  
  private ManagementPageMappingMocker managementPageMappingMocker = new ManagementPageMappingMocker();
  private ManagementTagMocker managementTagMocker = new ManagementTagMocker();
  private ManagementCategoryMocker managementCategoryMocker = new ManagementCategoryMocker();
  private ManagementPageMocker managementPageMocker = new ManagementPageMocker();
  private ManagementPostMocker managementPostMocker = new ManagementPostMocker();
  private ManagementShortlinkMocker managementShortlinkMocker = new ManagementShortlinkMocker();
  private ManagementAnnouncementMocker managementAnnouncementMocker = new ManagementAnnouncementMocker();
  private ManagementBannerMocker managementBannerMocker = new ManagementBannerMocker();
  private ManagementFragmentMocker managementFragmentMocker = new ManagementFragmentMocker();
  private ManagementMediaMocker managementMediaMocker = new ManagementMediaMocker();
  private ManagementTileMocker managementTileMocker = new ManagementTileMocker();
  private ManagementMenuMocker managementMenuMocker = new ManagementMenuMocker();
  
  private PtvServiceMocker ptvServiceMocker = new PtvServiceMocker();
  private PtvServiceChannelMocker ptvServiceChannelMocker = new PtvServiceChannelMocker();
  private PtvOrganizationMocker ptvOrganizationMocker = new PtvOrganizationMocker();
  
  @After
  public void afterEveryTest() {
    setLog4jLevel(Level.OFF);
    
    addServerLogEntry(String.format("### Test %s end ###", testName.getMethodName()));
    
    deleteSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING);
    clearTasks();
    
    managementMenuMocker.endMock();
    managementPageMappingMocker.endMock();
    managementPageMocker.endMock();
    managementPostMocker.endMock();
    managementShortlinkMocker.endMock();
    managementAnnouncementMocker.endMock();
    managementBannerMocker.endMock();
    managementFragmentMocker.endMock();
    managementMediaMocker.endMock();
    managementTileMocker.endMock();
    managementCategoryMocker.endMock();
    managementTagMocker.endMock();
    ptvServiceMocker.stop();
    ptvServiceChannelMocker.stop();
    ptvOrganizationMocker.stop();

    deleteIdentifiers();   
    deleteSystemSettings();
  }
  
  public void startMocks() { 
    createSystemSettings();
    
    ptvOrganizationMocker.start();
    ptvServiceChannelMocker.start();
    ptvServiceMocker.start();
    kuntarekryMocker.startMock();
    casemMocker.startMock();
    managementPageMocker.startMock();
    managementPostMocker.startMock();
    managementShortlinkMocker.startMock();
    managementAnnouncementMocker.startMock();
    managementBannerMocker.startMock();
    managementFragmentMocker.startMock();
    managementMediaMocker.startMock();
    managementTileMocker.startMock();
    managementPageMappingMocker.startMock();
    managementCategoryMocker.startMock();
    managementTagMocker.startMock();
    managementMenuMocker.startMock();
    
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
  
  public CasemMocker getCasemMocker() {
    return casemMocker;
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
  
  public PtvServiceChannelMocker getPtvServiceChannelMocker() {
    return ptvServiceChannelMocker;
  }
  
  public PtvOrganizationMocker getPtvOrganizationMocker() {
    return ptvOrganizationMocker;
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

  protected String getPageIdByPath(String organizationId, String path) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages?path=%s", organizationId, path))
        .body()
        .jsonPath()
        .getString(String.format("id[%d]", 0));
  }

  protected Page getPageByPath(String organizationId, String path) {
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/pages?path=%s", organizationId, path))
        .body()
        .jsonPath()
        .getObject("[0]", Page.class);
  }
  
  protected String getPageImageId(String organizationId, String pageId, int index) {
    return given() 
        .baseUri(getApiBasePath())
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
    return given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/organizations/{organizationId}/pages/{pageId}/content", organizationId, pageId)
        .body()
        .as(LocalizedValue[].class);
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
  
  protected String getElectronicChannelId(int index) throws InterruptedException {
    waitApiListCount("/electronicServiceChannels", 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getPhoneChannelId(int index) throws InterruptedException {
    waitApiListCount("/phoneServiceChannels", 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getPrintableFormChannelId(int index) throws InterruptedException {
    waitApiListCount("/printableFormServiceChannels", 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getServiceLocationChannelId(int index) throws InterruptedException {
    waitApiListCount("/serviceLocationServiceChannels", 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  protected String getWebPageChannelId(int index) throws InterruptedException {
    waitApiListCount("/webPageServiceChannels", 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
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
  
  protected void assertPageInPath(String path, String expectedSlug, String expectedParentId) {
    given() 
      .baseUri(getApiBasePath())
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
    given() 
      .baseUri(getApiBasePath())
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
  
  private void setLog4jLevel(Level level) {
    LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    Configuration config = loggerContext.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
    loggerConfig.setLevel(level);
    loggerContext.updateLoggers();
  }
  
  private void createSystemSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), "/ptv"));
  }
  
  private void deleteSystemSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
}