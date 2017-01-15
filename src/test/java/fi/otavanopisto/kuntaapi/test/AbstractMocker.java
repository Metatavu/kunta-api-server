package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Abstract base class for all mockers
 * 
 * @author Antti Leppä
 */
public class AbstractMocker {

  private static final String CONTENT_SIZE = "Content-Size";

  private static final String CONTENT_TYPE = "Content-Type";

  private static final String FAILED_TO_READ_MOCK_FILE = "Failed to read mock file";

  private static Logger logger = Logger.getLogger(AbstractMocker.class.getName());

  private List<StringGetMock> stringMocks;
  private List<BinaryGetMock> binaryMocks;
  private List<String> notFounds;
  
  /**
   * Constructor
   */
  public AbstractMocker() {
    stringMocks = new ArrayList<>();
    binaryMocks = new ArrayList<>();
    notFounds = new ArrayList<>();
  }
  
  public AbstractMocker mockNotFound(String path) {
    notFounds.add(path);
    return this;
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
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      stringMocks.add(new StringGetMock(path, "application/json", objectMapper.writeValueAsString(object), queryParams));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize mock JSON object", e);
      fail(e.getMessage());
    }
  }
  
  /**
   * Mocks XML response for GET request on path
   * 
   * @param path path
   * @param object JSON object
   */
  public void mockGetXML(String path, Object object, Map<String, String> queryParams) {
    try {
      XmlMapper xmlMapper = new XmlMapper();
      xmlMapper.registerModule(new JavaTimeModule());
      stringMocks.add(new StringGetMock(path, "text/xml", xmlMapper.writeValueAsString(object), queryParams));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize mock JSON object", e);
      fail(e.getMessage());
    }
  }
  
  protected String readFile(String file) {
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return IOUtils.toString(stream);
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  protected <T> T readXMLFile(String file, Class <T> type){
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.registerModule(new JavaTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return xmlMapper.readValue(stream, type);
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  protected <T> T readXMLFile(String file, TypeReference<T> typeReference){
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.registerModule(new JavaTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return xmlMapper.readValue(stream, typeReference);
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  protected <T> T readJSONFile(String file, Class <T> type){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return objectMapper.readValue(stream, type);
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    return null;
  }
  
  /**
   * Starts mocking requests
   */
  public void startMock() {
    for (String path : notFounds) {
      createNotFoundMock(path);
    }
    
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
    stubFor(head(urlPathEqualTo(path))
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, type)
      .withHeader(CONTENT_SIZE, String.valueOf(binary.length))
    ));
    
    stubFor(get(urlPathEqualTo(path))
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, type)
      .withBody(binary)
    ));
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
        .withHeader(CONTENT_TYPE, type)
        .withBody(content)));
  }
  
  private void createNotFoundMock(String path) {
    MappingBuilder mappingBuilder = get(urlPathEqualTo(path));
    
    stubFor(mappingBuilder
        .willReturn(aResponse()
        .withStatus(404)));
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