package fi.otavanopisto.kuntaapi.test;

import java.util.HashMap;
import java.util.Map;

import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.ApiResponse;

/**
 * Mocked for te-palvelut.fi API
 * 
 * @author Antti Leppä
 */
public class TptMocker extends AbstractMocker {

  /**
   * Mocks area search tpt endpoint
   * 
   * @param area area
   * @param file JSON file
   * @return self
   */
  public TptMocker mockAreaSearch(String area, String file) {
    ApiResponse apiResponse = readJSONFile(file, ApiResponse.class);
    Map<String, String> query = new HashMap<>();
    query.put("alueet", area);
    mockGetJSON("/tpt-api/tyopaikat", apiResponse, null);
    return this;
  }
  
}
