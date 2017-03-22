package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.metatavu.restfulptv.client.model.Service;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvServiceMocker extends AbstractBaseMocker {

  private static final String SERVICES_PATH = String.format("%s/services", AbstractIntegrationTest.BASE_URL);
  private static final String CHANNEL_TEMPLATE = "%s/%s";
  
  private ResourceMocker<String, Service> serviceMocker = new ResourceMocker<>();

  public RestfulPtvServiceMocker() {
    serviceMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(SERVICES_PATH));
  }
  
  @Override
  public void startMock() {
    super.startMock();
    serviceMocker.start();
  }
  
  @Override
  public void endMock() {
    serviceMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks resources
   * 
   * @param ids resource ids
   * @return mocker mocker
   */
  public RestfulPtvServiceMocker mockServices(String... ids) {
    try {
      for (String id : ids) {
        if (!serviceMocker.isMocked(id)) {
          mockService(readServiceFromJSONFile(String.format("services/%s.json", id)));
        } else {
          serviceMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }

    return this;
  }
  
  /**
   * Unmocks resources
   * 
   * @param ids resource ids
   * @return mocker mocker
   */
  public RestfulPtvServiceMocker unmockServices(String... ids) {
    for (String id : ids) {
      serviceMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  private void mockService(Service resource) throws JsonProcessingException {
    String id = resource.getId();
    String path = String.format(CHANNEL_TEMPLATE, SERVICES_PATH, id);
    serviceMocker.add(id, resource, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Service readServiceFromJSONFile(String file) {
    return readJSONFile(file, Service.class);
  }
  
}
