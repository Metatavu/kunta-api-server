package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Tile;

@SuppressWarnings ("squid:S1166")
public class ManagementTileMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/wp/v2/tile";
  
  private ManagementResourceMocker<Integer, Tile> tileMocker = new ManagementResourceMocker<>();

  public ManagementTileMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    tileMocker.start();
  }
  
  @Override
  public void endMock() {
    tileMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management tiles
   * 
   * @param ids tile ids
   * @return mocker
   */
  public ManagementTileMocker mockTiles(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!tileMocker.isMocked(id)) {
          Tile tile = readTileFromJSONFile(String.format("management/tiles/%d.json", id));
          mockTile(tile);
        } else {
          tileMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management tiles
   * 
   * @param ids tile ids
   * @return mocker
   */
  public ManagementTileMocker unmockTiles(Integer... ids) {
    for (Integer id : ids) {
      tileMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementTileMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    tileMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    tileMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockTile(Tile tile) throws JsonProcessingException {
    Integer tileId = tile.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, tileId);
    tileMocker.add(tileId, tile, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as tile object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Tile readTileFromJSONFile(String file) {
    return readJSONFile(file, Tile.class);
  }
  
}
