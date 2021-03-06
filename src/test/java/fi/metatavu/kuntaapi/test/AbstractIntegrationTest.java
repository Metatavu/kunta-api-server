package fi.metatavu.kuntaapi.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.specification.RequestSpecification;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.kuntaapi.test.json.JSONMatcher;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.persistence.model.clients.AccessType;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;

/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  public static final String BASE_URL = "/v1";
  private static final Logger logger = Logger.getLogger(AbstractIntegrationTest.class.getName());

  protected static final String IMAGE_JPEG = "image/jpeg";
  protected static final String IMAGE_GIF = "image/gif";
  protected static final String IMAGE_PNG = "image/png";

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
  private PtvServiceMocker ptvServiceMocker = new PtvServiceMocker();
  private PtvServiceChannelMocker ptvServiceChannelMocker = new PtvServiceChannelMocker();
  private PtvOrganizationMocker ptvOrganizationMocker = new PtvOrganizationMocker();
  private PtvCodesMocker ptvCodesMocker = new PtvCodesMocker();
  private ManagementPostMenuOrderMocker managementPostMenuOrderMocker = new ManagementPostMenuOrderMocker();
  private FmiWeatherAlertsWfsMocker fmiWeatherAlertsWfsMocker = new FmiWeatherAlertsWfsMocker();

  @Before
  public void beforeEveryTest() throws IOException {
    if (dropIdentifiersBefore()) {
      deleteIdentifiers(getPurgeOrganizations());
    }
  }
  
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
    ptvServiceMocker.stop();
    ptvServiceChannelMocker.stop();
    ptvOrganizationMocker.stop();
    ptvCodesMocker.endMock();
    managementPostMenuOrderMocker.endMock();
    fmiWeatherAlertsWfsMocker.endMock();

    deleteOrganizationPermissions();
    
    if (dropIdentifiersAfter()) {
      deleteIdentifiers(getPurgeOrganizations());
    }
    
    deleteSystemSettings();
  }
  
  @SuppressWarnings ("squid:S00108")
  private void deleteIndices() {
    try {
      String types = "service,electronic-service-channel,phone-service-channel,printable-form-service-channel,service-location-service-channel,web-page-service-channel";
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
  
  /**
   * Returns whether identifiers should be dropped before every test. 
   * 
   * Defaults to false
   * 
   * @return whether identifiers should be dropped before every test
   */
  protected boolean dropIdentifiersBefore() {
    return false;
  }

  /**
   * Returns whether identifiers should be dropped after every test. 
   * 
   * Defaults to true
   * 
   * @return whether identifiers should be dropped after every test
   */
  protected boolean dropIdentifiersAfter() {
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
    managementPostMenuOrderMocker.startMock();
    fmiWeatherAlertsWfsMocker.startMock();
    
    insertSystemSetting(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING, "true");
    
    addServerLogEntry(String.format("### Test %s start ###", testName.getMethodName()));
    setLog4jLevel(Level.WARN);
  }
  
  public PtvServiceMocker getPtvServiceMocker() {
    return ptvServiceMocker;
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
  
  public PtvServiceChannelMocker getPtvServiceChannelMocker() {
    return ptvServiceChannelMocker;
  }
  
  public PtvOrganizationMocker getPtvOrganizationMocker() {
    return ptvOrganizationMocker;
  }
  
  public PtvCodesMocker getPtvCodesMocker() {
    return ptvCodesMocker;
  }
  
  public ManagementPostMenuOrderMocker getManagementPostMenuOrderMocker() {
    return managementPostMenuOrderMocker;
  }

  public FmiWeatherAlertsWfsMocker getFmiWeatherAlertsWfsMocker() {
    return fmiWeatherAlertsWfsMocker;
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
  
  private Long findClientDatabaseIdByAccessType(AccessType accessType) {
    return findClientDatabaseIdByClientId(getClientId(accessType));
  }
  
  private Long findClientDatabaseIdByClientId(String clientId) {
    Connection connection = getConnection();
    
    try {
      try {
        connection.setAutoCommit(true);
        PreparedStatement statement = connection.prepareStatement("select id from Client where clientId = ?");
        try {
          statement.setObject(1, clientId);
          statement.execute();
          try (ResultSet resultSet = statement.getResultSet()) {
            if (resultSet.next()) {
              return resultSet.getLong(1);
            }
          }
        } finally {
          statement.close();
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    return null;
 }
  
  private Long findIdentifierIdByKuntaApiId(String kuntaApiId) {
    Connection connection = getConnection();
    
    try {
      try {
        connection.setAutoCommit(true);
        PreparedStatement statement = connection.prepareStatement("select id from Identifier where kuntaApiId = ?");
        try {
          statement.setObject(1, kuntaApiId);
          statement.execute();
          try (ResultSet resultSet = statement.getResultSet()) {
            if (resultSet.next()) {
              return resultSet.getLong(1);
            }
          }
        } finally {
          statement.close();
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    return null;
 }
  
  protected void grantOrganizationPermission(AccessType accessType, String organizationKuntaApiId, ClientOrganizationPermission permission) {
    Long organizationId = findIdentifierIdByKuntaApiId(organizationKuntaApiId);
    Long clientDatabaseId = findClientDatabaseIdByAccessType(accessType);
    executeInsert("insert into ClientOrganizationPermissionGrant (client_id, organizationIdentifier_id, permission) values (?, ?, ?)", clientDatabaseId, organizationId, permission.name());
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
    return givenAuthenticated(AccessType.UNRESTRICTED, false);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read only access to API
   * 
   * @return REST assurred request specification autheticated with test client with read only access to API
   */
  protected RequestSpecification givenReadonly() {
    return givenAuthenticated(AccessType.READ_ONLY, false);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read write access to API
   * 
   * @return REST assurred request specification autheticated with test client with read write access to API
   */
  protected RequestSpecification givenReadWrite() {
    return givenAuthenticated(AccessType.READ_WRITE, false);
  }

    /**
   * Returns REST assurred request specification autheticated with test client with unrestricted access to API
   * 
   * @return REST assurred request specification autheticated with test client with unrestricted access to API
   */
  protected RequestSpecification givenUnrestrictedCompabilityMode() {
    return givenAuthenticated(AccessType.UNRESTRICTED, true);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read only access to API
   * 
   * @return REST assurred request specification autheticated with test client with read only access to API
   */
  protected RequestSpecification givenReadonlyCompabilityMode() {
    return givenAuthenticated(AccessType.READ_ONLY, true);
  }

  /**
   * Returns REST assurred request specification autheticated with test client with read write access to API
   * 
   * @return REST assurred request specification autheticated with test client with read write access to API
   */
  protected RequestSpecification givenReadWriteCompabilityMode() {
    return givenAuthenticated(AccessType.READ_WRITE, true);
  }
  
  /**
   * Returns REST assurred request specification autheticated with test client of given access type
   * 
   * @param accessType access type
   * @param compabilityMode use PTV-7 compability mode header
   * 
   * @return REST assurred request specification autheticated with test client of given access type
   */
  protected RequestSpecification givenAuthenticated(AccessType accessType, boolean compabilityMode) {
    return given()
      .header("Kunta-API-PTV7-Compatibility", compabilityMode ? "true" : "false")
      .baseUri(getApiBasePath())
      .auth().preemptive().basic(getClientId(accessType), getClientSecret(accessType));
  }

  protected void flushCache() {
    givenUnrestricted()
      .get("/system/jpa/cache/flush")
      .then();
  }
  
  protected void clearTasks() {
    try {
      executeDelete("delete from Task");
    } catch (Exception e) {
      logger.log(java.util.logging.Level.SEVERE, "Purging tasks failed", e);
    }
  }

  @SuppressWarnings ({"squid:S1166", "squid:S00108", "squid:S2925", "squid:S106"})
  protected void waitNewsArticleSlug(String organizationId, int index, String sortBy, String slug) throws InterruptedException {
    int counter = 0;
    long timeout = System.currentTimeMillis() + (60 * 1000 * 5);
    while (true) {
      counter++;
      Thread.sleep(1000);
      try {
        String currentSlug = getNewsArticleSlug(organizationId, sortBy, index);
        if (slug.equals(currentSlug)) {
          return;
        }
        
        if (System.currentTimeMillis() > timeout) {
          fail(String.format("Timeout waiting for news article %d to have slug %s", index, slug));
        }
        
        if ((counter % 10) == 0) {
          System.out.println(String.format("... still waiting for news article %d to have slug %s (current slug is %s)", index, slug, currentSlug));
        }
        
      } catch (JsonPathException e) {
        
      }
    }
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
  
  /**
   * Returns environmental warning id from API by index
   * 
   * @param organizationId organization id
   * @param index index
   * @return environmental warning id
   */
  protected String getEnvironmentalWarningId(String organizationId, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/environmentalWarnings", organizationId))
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
  
  protected String getNewsArticleSlug(String organizationId, String sortBy, int index) {
    return givenReadonly()
        .contentType(ContentType.JSON)
        .get(String.format("/organizations/%s/news?sortBy=%s", organizationId, sortBy))
        .body()
        .jsonPath()
        .getString(String.format("slug[%d]", index));
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
  
  protected Service getService(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/services", waitCount);
    
    try (InputStream serviceDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<Service> results = getObjectMapper().readValue(serviceDataStream, new TypeReference<List<Service>>() {});
      return results.get(0);
    }
  }
  
  protected ElectronicServiceChannel getElectronicServiceChannel(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/electronicServiceChannels", waitCount);
    
    try (InputStream channelDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/electronicServiceChannels?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<ElectronicServiceChannel> results = getObjectMapper().readValue(channelDataStream, new TypeReference<List<ElectronicServiceChannel>>() {});
      return results.get(0);
    }
  }
  
  protected PhoneServiceChannel getPhoneServiceChannel(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/phoneServiceChannels", waitCount);
    
    try (InputStream channelDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/phoneServiceChannels?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<PhoneServiceChannel> results = getObjectMapper().readValue(channelDataStream, new TypeReference<List<PhoneServiceChannel>>() {});
      return results.get(0);
    }
  }
  
  protected PrintableFormServiceChannel getPrintableFormServiceChannel(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/printableFormServiceChannels", waitCount);
    
    try (InputStream channelDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/printableFormServiceChannels?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<PrintableFormServiceChannel> results = getObjectMapper().readValue(channelDataStream, new TypeReference<List<PrintableFormServiceChannel>>() {});
      return results.get(0);
    }
  }
  
  protected ServiceLocationServiceChannel getServiceLocationServiceChannel(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/serviceLocationServiceChannels", waitCount);
    
    try (InputStream channelDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<ServiceLocationServiceChannel> results = getObjectMapper().readValue(channelDataStream, new TypeReference<List<ServiceLocationServiceChannel>>() {});
      return results.get(0);
    }
  }
  
  protected WebPageServiceChannel getWebPageChannel(int index, int waitCount) throws IOException, InterruptedException {
    waitApiListCount("/webPageServiceChannels", waitCount);
    
    try (InputStream channelDataStream = givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/webPageServiceChannels?firstResult=%d&maxResults=1", index))
      .body()
      .asInputStream()) {      
      List<WebPageServiceChannel> results = getObjectMapper().readValue(channelDataStream, new TypeReference<List<WebPageServiceChannel>>() {});
      return results.get(0);
    }
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
      .get(String.format("%s?firstResult=%d&maxResults=20", basePath, maxResults))
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
  
  @SuppressWarnings ("squid:S2925")
  protected void waitMs(int ms) {
    if (ms > 0) {
      try {
        Thread.sleep(ms);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Returns Hamcrest Matcher for checking that JSONs equal
   * 
   * @param expected expected JSON file path
   * @param customizations JSONAssert customizations
   * @return Matcher
   * @throws IOException when file reading fails
   */
  protected Matcher<Object> jsonEqualsFile(final String expectedFile, Customization... customizations) throws IOException {
    return jsonEquals(readFileAsString(expectedFile), customizations);
  }

  /**
   * Returns Hamcrest Matcher for checking that JSONs equal
   * 
   * @param expected expected JSON string
   * @param customizations JSONAssert customizations
   * @return Matcher
   */
  protected Matcher<Object> jsonEquals(final String expected, Customization... customizations) {
    return new JSONMatcher(expected, customizations);
  }
  
  /**
   * Asserts that actual JSON string equals to expected string
   * 
   * @param expected expected JSON string
   * @param actual actual JSON string
   * @param customizations customization rules (optional)
   * @throws JSONException when JSON parsing fails
   */
  protected void assertJSONEquals(String expected, String actual, Customization... customizations) throws JSONException {
    CustomComparator customComparator = new CustomComparator(JSONCompareMode.LENIENT, customizations);
    JSONAssert.assertEquals(expected, actual, customComparator);
  }
  
  /**
   * Asserts that actual JSON string equals to expected string from a file
   * 
   * @param expectedFile file containing expected JSON string
   * @param actual actual JSON string
   * @param customizations customization rules (optional)
   * @throws JSONException when JSON parsing fails
   * @throws IOException when file reading fails
   */
  protected void assertJSONFileEquals(String expectedFile, String actual, Customization... customizations) throws JSONException, IOException {
    CustomComparator customComparator = new CustomComparator(JSONCompareMode.LENIENT, customizations);
    JSONAssert.assertEquals(readFileAsString(expectedFile), actual, customComparator);
  }

  /**
   * Reads test file contents as string
   * 
   * @param path file path
   * @return file contents
   * @throws IOException when file reading fails
   */
  protected String readFileAsString(String path) throws IOException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream(path)) {
      return IOUtils.toString(fileStream, "UTF-8");
    }
  }
  
  /**
   * Calculates contents md5 from a resource
   * 
   * @param resourceName resource name
   * @return resource contents md5
   * @throws IOException thrown when file reading fails
   */
  protected String getResourceMd5(String resourceName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream fileStream = classLoader.getResourceAsStream(resourceName)) {
      return DigestUtils.md5Hex(fileStream);
    }    
  }
  
  private void deleteOrganizationPermissions() {
    try {
      executeDelete("delete from ClientOrganizationPermissionGrant");
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }
  
  private void createSystemSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), "/ptv"));
  }
  
  private void deleteSystemSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
}