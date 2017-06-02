package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Incident;

@SuppressWarnings ({"squid:S1166", "squid:S1075"})
public class ManagementIncidentMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/incident";
  
  private ManagementResourceMocker<Integer, Incident> incidentMocker = new ManagementResourceMocker<>();

  public ManagementIncidentMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    incidentMocker.start();
  }
  
  @Override
  public void endMock() {
    incidentMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management incidents
   * 
   * @param ids incident ids
   * @return mocker
   */
  public ManagementIncidentMocker mockIncidents(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!incidentMocker.isMocked(id)) {
          Incident incident = readIncidentFromJSONFile(String.format("management/incidents/%d.json", id));
          mockIncident(incident);
        } else {
          incidentMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management incidents
   * 
   * @param ids incident ids
   * @return mocker
   */
  public ManagementIncidentMocker unmockIncidents(Integer... ids) {
    for (Integer id : ids) {
      incidentMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementIncidentMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    incidentMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    incidentMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockIncident(Incident incident) throws JsonProcessingException {
    Integer incidentId = incident.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, incidentId);
    incidentMocker.add(incidentId, incident, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as incident object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Incident readIncidentFromJSONFile(String file) {
    return readJSONFile(file, Incident.class);
  }
  
}
