package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.restfulptv.client.model.Organization;
import fi.otavanopisto.restfulptv.client.model.Service;

/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {

  public static final String BASE_URL = "/v1";
  
  private static Logger logger = Logger.getLogger(AbstractTest.class.getName());

  private RestFulPtvMocker ptvMocker = new RestFulPtvMocker();
  
  public RestFulPtvMocker getPtvMocker() {
    return ptvMocker;
  }
  
  protected void flushCache() {
    given()
      .baseUri(getApiBasePath())
      .get("/system/jpa/cache/flush")
      .then()
      .statusCode(200);
  }
  /**
   * Abstract base class for all mockers
   * 
   * @author Antti Leppä
   */
  public class AbstractMocker {
    
    private List<StringGetMock> stringMocks;
    private List<BinaryGetMock> binaryMocks;
    
    /**
     * Constructor
     */
    public AbstractMocker() {
      stringMocks = new ArrayList<>();
      binaryMocks = new ArrayList<>();
    }
    
    /**
     * Mocks binary response for GET request on path
     * 
     * @param path path
     * @param type response content type 
     * @param binaryFile path of mocked file
     */
    public void mockGetBinary(String path, String type, String binaryFile) {
      try (InputStream binaryStream = getClass().getClassLoader().getResourceAsStream(binaryFile)) {
        binaryMocks.add(new BinaryGetMock(path, type, IOUtils.toByteArray(binaryStream)));
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to read mock binary file", e);
        fail(e.getMessage());
      }
    }
    
    /**
     * Mocks string response for GET request on path
     * 
     * @param path path
     * @param type response content type 
     * @param content response content
     */
    public void mockGetString(String path, String type, String content) {
      stringMocks.add(new StringGetMock(path, type, content, null));
    }
    
    /**
     * Mocks string response for GET request on path
     * 
     * @param path path
     * @param type response content type 
     * @param queryParams query params for the reuqest
     * @param content response content
     */
    public void mockGetString(String path, String type, String content, Map<String, String> queryParams) {
      stringMocks.add(new StringGetMock(path, type, content, queryParams));
    }
    
    /**
     * Mocks JSON response for GET request on path
     * 
     * @param path path
     * @param object JSON object
     */
    public void mockGetJSON(String path, Object object, Map<String, String> queryParams) {
      try {
        stringMocks.add(new StringGetMock(path, "application/json", new ObjectMapper().writeValueAsString(object), queryParams));
      } catch (JsonProcessingException e) {
        logger.log(Level.SEVERE, "Failed to serialize mock JSON object", e);
        fail(e.getMessage());
      }
    }
    
    /**
     * Reads JSON file as organization object
     * 
     * @param file path to JSON file
     */    
    public Organization readOrganizationFromJSONFile(String file) {
      return readJSONFile(file, Organization.class);
    }
    
    /**
     * Reads JSON file as organization object
     * 
     * @param file path to JSON file
     */    
    public Service readServiceFromJSONFile(String file) {
      return readJSONFile(file, Service.class);
    }
    
    private <T> T readJSONFile(String file, Class <T> type){
      ObjectMapper objectMapper = new ObjectMapper();
      try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
        return objectMapper.readValue(stream, type);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to read mock file", e);
        fail(e.getMessage());
      }
      return null;
    }
    
    /**
     * Starts mocking requests
     */
    public void startMock() {
      for (StringGetMock stringMock : stringMocks) {
        createStringMock(stringMock.getPath(), stringMock.getType(), stringMock.getContent(), stringMock.getQueryParams());
      }
      
      for (BinaryGetMock binaryMock : binaryMocks) {
        createBinaryMock(binaryMock.getPath(), binaryMock.getType(), binaryMock.getContent());
      }
    }

    /**
     * Ends mocking
     */
    public void endMock() {
      WireMock.reset();
    }
    
    private void createBinaryMock(String path, String type, byte[] binary) {
      stubFor(get(urlPathEqualTo(path))
        .willReturn(aResponse()
        .withHeader("Content-Type", type)
        .withBody(binary)));
    }

    private void createStringMock(String path, String type, String content, Map<String, String> queryParams) {
      MappingBuilder mappingBuilder = get(urlPathEqualTo(path));
      
      if (queryParams != null) {
        for (Entry<String, String> queryParam : queryParams.entrySet()) {
          mappingBuilder.withQueryParam(queryParam.getKey(), equalTo(queryParam.getValue()));
        }
      }
      
      stubFor(mappingBuilder
          .willReturn(aResponse()
          .withHeader("Content-Type", type)
          .withBody(content)));
    }
    
    private class StringGetMock {

      private String path;
      private String type;
      private String content;
      private Map<String, String> queryParams;
     
      public StringGetMock(String path, String type, String content, Map<String, String> queryParams) {
        this.path = path;
        this.type = type;
        this.content = content;
        this.queryParams = queryParams;
      }
      
      public String getPath() {
        return path;
      }
     
      public String getContent() {
        return content;
      }
      
      public String getType() {
        return type;
      }
      
      public Map<String, String> getQueryParams() {
        return queryParams;
      }
    }
    
    private class BinaryGetMock {

      private String path;
      private String type;
      private byte[] content;
     
      public BinaryGetMock(String path, String type, byte[] content) {
        this.path = path;
        this.type = type;
        this.content = content;
      }
      
      public String getPath() {
        return path;
      }
     
      public byte[] getContent() {
        return content;
      }
      
      public String getType() {
        return type;
      }
    }

  }

  public class RestFulPtvMocker extends AbstractMocker {
    
    private List<Organization> organizationsList;
    private List<Service> servicesList;
    
    public RestFulPtvMocker() {
      organizationsList = new ArrayList<>();
      servicesList = new ArrayList<>();
    }

    public RestFulPtvMocker mockOrganizations(String... ids) {
      for (String id : ids) {
        Organization organization = readOrganizationFromJSONFile(String.format("organizations/%s.json", id));
        mockGetJSON(String.format("%s/organizations/%s", BASE_URL, id), organization, null);
        organizationsList.add(organization);
      }     
      return this;
    }
    
    public RestFulPtvMocker mockServices(String... ids) {
      for (String id : ids) {
        Service service = readServiceFromJSONFile(String.format("services/%s.json", id));
        mockGetJSON(String.format("%s/services/%s", BASE_URL, id), service, null);
        servicesList.add(service);
      }     
      return this;
    }
    
    @Override
    public void startMock() {
      Map<String, String> pageQuery = new HashMap<>();
      pageQuery.put("firstResult", "0");
      pageQuery.put("maxResults", "20");

      mockGetJSON(String.format("%s/organizations", BASE_URL), organizationsList, pageQuery);
      mockGetJSON(String.format("%s/organizations", BASE_URL), organizationsList, null);

      mockGetJSON(String.format("%s/services", BASE_URL), servicesList, pageQuery);
      mockGetJSON(String.format("%s/services", BASE_URL), servicesList, null);
      
      
      super.startMock();
    }
  }
  
  protected void waitApiListCount(String path, int count) throws InterruptedException {
    long timeout = System.currentTimeMillis() + (120 * 1000);
    while (true) {
      Thread.sleep(1000);
      
      int listCount = countApiList(path);
      if (listCount == count) {
        return;
      }
      
      if (System.currentTimeMillis() > timeout) {
        fail(String.format("Timeout waiting for %s to have count %d", path, count));
      }
    }
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
    .body("id.size()", is(2));
  
  given() 
    .baseUri(getApiBasePath())
    .contentType(ContentType.JSON)
    .get(String.format("%s?firstResult=2", basePath))
    .then()
    .assertThat()
    .statusCode(200)
    .body("id.size()", is(1));
  
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
}