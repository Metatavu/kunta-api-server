package fi.metatavu.kuntaapi.test;

import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.FeatureCollection;

/**
 * Mocker for FMI Weather alerts WFS API
 * 
 * @author Antti Leppä
 */
public class FmiWeatherAlertsWfsMocker extends AbstractMocker {

  /**
   * Starts mocking
   * 
   * @param path mocked API path
   * @param file mock file
   * @return mocker
   */
  public FmiWeatherAlertsWfsMocker mockWfs(String path, String file) {
    FeatureCollection featureCollection = readJSONFile(file, FeatureCollection.class);
    mockGetJSON(path, featureCollection, null);
    return this;
  }
  
}
