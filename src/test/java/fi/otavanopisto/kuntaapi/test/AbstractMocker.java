package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import fi.otavanopisto.restfulptv.client.model.ElectronicChannel;
import fi.otavanopisto.restfulptv.client.model.Organization;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;
import fi.otavanopisto.restfulptv.client.model.PhoneChannel;
import fi.otavanopisto.restfulptv.client.model.PrintableFormChannel;
import fi.otavanopisto.restfulptv.client.model.Service;
import fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel;
import fi.otavanopisto.restfulptv.client.model.WebPageChannel;

/**
 * Abstract base class for all mockers
 * 
 * @author Antti Leppä
 */
public class AbstractMocker {

  private static Logger logger = Logger.getLogger(AbstractMocker.class.getName());

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
  
  /**
   * Reads JSON file as electronic service channel object
   * 
   * @param file path to JSON file
   */    
  public ElectronicChannel readElectronicChannelFromJSONFile(String file) {
    return readJSONFile(file, ElectronicChannel.class);
  }
  
  /**
   * Reads JSON file as phone service channel object
   * 
   * @param file path to JSON file
   */    
  public PhoneChannel readPhoneChannelFromJSONFile(String file) {
    return readJSONFile(file, PhoneChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   */    
  public PrintableFormChannel readPrintableFormChannelFromJSONFile(String file) {
    return readJSONFile(file, PrintableFormChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   */    
  public ServiceLocationChannel readServiceLocationChannelFromJSONFile(String file) {
    return readJSONFile(file, ServiceLocationChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   */    
  public WebPageChannel readWebPageChannelFromJSONFile(String file) {
    return readJSONFile(file, WebPageChannel.class);
  }
  
  public OrganizationService readOrganizationServiceFromJSONFile(String file) {
    return readJSONFile(file, OrganizationService.class);
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