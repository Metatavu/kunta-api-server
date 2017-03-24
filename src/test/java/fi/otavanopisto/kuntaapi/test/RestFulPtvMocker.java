package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.metatavu.restfulptv.client.model.Organization;
import fi.metatavu.restfulptv.client.model.Service;
import fi.metatavu.restfulptv.client.model.StatutoryDescription;

public class RestFulPtvMocker extends AbstractMocker {
  
  private List<StatutoryDescription> statutoryDescriptionsList;
  
  public RestFulPtvMocker() {
    statutoryDescriptionsList = new ArrayList<>();
  }

  public RestFulPtvMocker mockStatutoryDescriptions(String...ids) {
    for (String id : ids) {
      StatutoryDescription statutoryDescription = readStatutoryDescriptionFromJSONFile(String.format("statutorydescriptions/%s.json", id));
      mockGetJSON(String.format("%s/statutoryDescriptions/%s", AbstractIntegrationTest.BASE_URL, id), statutoryDescription, null);
      statutoryDescriptionsList.add(statutoryDescription);
    }
    
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

  @Override
  public void startMock() {
    Map<String, String> pageQuery = new HashMap<>();
    pageQuery.put("firstResult", "0");
    pageQuery.put("maxResults", "20");

    mockGetJSON(String.format("%s/statutoryDescriptions", AbstractIntegrationTest.BASE_URL), statutoryDescriptionsList, pageQuery);
    mockGetJSON(String.format("%s/statutoryDescriptions", AbstractIntegrationTest.BASE_URL), statutoryDescriptionsList, null);

    super.startMock();
  }
}