package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.otavanopisto.restfulptv.client.model.Organization;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvOrganizationMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String ORGANIZATIONS_PATH = String.format("%s/organizations", AbstractIntegrationTest.BASE_URL);
  private static final String ORGANIZATION_SERVICES_PATH = String.format("%s/%%s/organizationServices", ORGANIZATIONS_PATH);

  private ResourceMocker<String, Organization> organizationMocker = new ResourceMocker<>();

  public RestfulPtvOrganizationMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    organizationMocker.start();
  }
  
  @Override
  public void endMock() {
    organizationMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvOrganizationMocker mockOrganizations(String... ids) {
    try {
      for (String id : ids) {
        if (!organizationMocker.isMocked(id)) {
          mockOrganization(readOrganizationFromJSONFile(String.format("organizations/%s.json", id)));
          
          ResourceMocker<String, OrganizationService> organizationServicesMocker = new ResourceMocker<>();
          organizationServicesMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(ORGANIZATION_SERVICES_PATH, id)));
          organizationMocker.addSubMocker(id, organizationServicesMocker);
          
        } else {
          organizationMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Mocks organization service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvOrganizationMocker mockOrganizationServices(String organizationId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, OrganizationService> organizationServicesMocker = (ResourceMocker<String, OrganizationService>) organizationMocker.getSubMocker(organizationId, 0);
      if (!organizationServicesMocker.isMocked(id)) {
        organizationServicesMocker.add(id, readOrganizationServiceFromJSONFile(String.format("organizationservices/%s.json", id)), urlPathEqualTo(String.format(PATH_TEMPLATE, String.format(ORGANIZATION_SERVICES_PATH, organizationId), id)));
      } else {
        organizationServicesMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Unmocks management organizations
   * 
   * @param ids organization ids
   * @return mocker
   */
  public RestfulPtvOrganizationMocker unmockOrganizations(String... ids) {
    for (String id : ids) {
      organizationMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public RestfulPtvOrganizationMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("maxResults", containing("20"));
    queryParams.put("firstResult", containing("0"));
    
    organizationMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(ORGANIZATIONS_PATH));
    organizationMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(ORGANIZATIONS_PATH), queryParams);

    return this;
  }
  
  private void mockOrganization(Organization organization) throws JsonProcessingException {
    String organizationId = organization.getId();
    String path = String.format(PATH_TEMPLATE, ORGANIZATIONS_PATH, organizationId);
    organizationMocker.add(organizationId, organization, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Organization readOrganizationFromJSONFile(String file) {
    return readJSONFile(file, Organization.class);
  }
  
  /**
   * Reads JSON file as organization service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */
  private OrganizationService readOrganizationServiceFromJSONFile(String file) {
    return readJSONFile(file, OrganizationService.class);
  }
  
}
