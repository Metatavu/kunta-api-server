package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fi.otavanopisto.restfulptv.client.model.Organization;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;
import fi.otavanopisto.restfulptv.client.model.Service;
import fi.otavanopisto.restfulptv.client.model.StatutoryDescription;

public class RestFulPtvMocker extends AbstractMocker {
  
  private List<Organization> organizationsList;
  private List<Service> servicesList;
  private List<StatutoryDescription> statutoryDescriptionsList;
  private Map<String, List<OrganizationService>> organizationServiceList;
  
  public RestFulPtvMocker() {
    organizationsList = new ArrayList<>();
    servicesList = new ArrayList<>();
    statutoryDescriptionsList = new ArrayList<>();
    organizationServiceList = new HashMap<>();
  }

  public RestFulPtvMocker mockOrganizations(String... ids) {
    for (String id : ids) {
      Organization organization = readOrganizationFromJSONFile(String.format("organizations/%s.json", id));
      mockGetJSON(String.format("%s/organizations/%s", AbstractIntegrationTest.BASE_URL, id), organization, null);
      organizationsList.add(organization);
      
      List<OrganizationService> channelList = organizationServiceList.get(id);
      if (channelList == null) {
        organizationServiceList.put(id, new ArrayList<>(ids.length));
      }
    }     
    
    return this;
  }
  
  public RestFulPtvMocker mockStatutoryDescriptions(String...ids) {
    for (String id : ids) {
      StatutoryDescription statutoryDescription = readStatutoryDescriptionFromJSONFile(String.format("statutorydescriptions/%s.json", id));
      mockGetJSON(String.format("%s/statutoryDescriptions/%s", AbstractIntegrationTest.BASE_URL, id), statutoryDescription, null);
      statutoryDescriptionsList.add(statutoryDescription);
    }
    
    return this;
  }
  
  public RestFulPtvMocker mockServices(String... ids) {
    for (String id : ids) {
      Service service = readServiceFromJSONFile(String.format("services/%s.json", id));
      mockGetJSON(String.format("%s/services/%s", AbstractIntegrationTest.BASE_URL, id), service, null);
      servicesList.add(service);
    }     
    
    return this;
  }

  public RestFulPtvMocker mockOrganizationServices(String organizationId, String... ids) {
    List<OrganizationService> channelList = organizationServiceList.get(organizationId);
    for (String id : ids) {
      OrganizationService channel = readOrganizationServiceFromJSONFile(String.format("organizationservices/%s.json", id));
      mockGetJSON(String.format("%s/organizations/%s/organizationServices/%s", AbstractIntegrationTest.BASE_URL, organizationId, id), channel, null);
      channelList.add(channel);
    }     
    
    organizationServiceList.put(organizationId, channelList);
    
    return this;
  }

  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public Organization readOrganizationFromJSONFile(String file) {
    return readJSONFile(file, Organization.class);
  }
  
  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public Service readServiceFromJSONFile(String file) {
    return readJSONFile(file, Service.class);
  }
  
  /**
   * Reads JSON file as StatutoryDescription object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public StatutoryDescription readStatutoryDescriptionFromJSONFile(String file) {
    return readJSONFile(file, StatutoryDescription.class);
  }

  /**
   * Reads JSON file as organization service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */
  public OrganizationService readOrganizationServiceFromJSONFile(String file) {
    return readJSONFile(file, OrganizationService.class);
  }
  
  @Override
  public void startMock() {
    Map<String, String> pageQuery = new HashMap<>();
    pageQuery.put("firstResult", "0");
    pageQuery.put("maxResults", "20");

    mockGetJSON(String.format("%s/organizations", AbstractIntegrationTest.BASE_URL), organizationsList, pageQuery);
    mockGetJSON(String.format("%s/organizations", AbstractIntegrationTest.BASE_URL), organizationsList, null);

    mockGetJSON(String.format("%s/services", AbstractIntegrationTest.BASE_URL), servicesList, pageQuery);
    mockGetJSON(String.format("%s/services", AbstractIntegrationTest.BASE_URL), servicesList, null);

    mockGetJSON(String.format("%s/statutoryDescriptions", AbstractIntegrationTest.BASE_URL), statutoryDescriptionsList, pageQuery);
    mockGetJSON(String.format("%s/statutoryDescriptions", AbstractIntegrationTest.BASE_URL), statutoryDescriptionsList, null);

    for (Entry<String, List<OrganizationService>> organizationServiceEntry : organizationServiceList.entrySet()) {
      mockGetJSON(String.format("%s/organizations/%s/organizationServices", AbstractIntegrationTest.BASE_URL, organizationServiceEntry.getKey()), organizationServiceEntry.getValue(), null);
    }

    super.startMock();
  }
}