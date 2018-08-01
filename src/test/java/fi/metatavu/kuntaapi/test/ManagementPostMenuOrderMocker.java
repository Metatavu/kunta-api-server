package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

import fi.metatavu.kuntaapi.server.integrations.management.client.model.PostMenuOrder;

/**
 * Mocker that is used to mock post menu order Wordpress plugin results
 * 
 * @author Antti Leppä
 */
public class ManagementPostMenuOrderMocker extends AbstractBaseMocker {
  
  @SuppressWarnings ("squid:S1075")
  private static final String PATH = "/wp-json/menuorder/v1/posts/%s";
  
  private Map<Integer, MappingBuilder> menuMappings;
  
  @Override
  public void startMock() {
    menuMappings.values().stream().forEach(WireMock::stubFor);
    
    super.startMock();
  }
  
  @Override
  public void endMock() {
    menuMappings.values().stream().forEach(WireMock::removeStub);
    
    super.endMock();
  }
  
  /**
   * Constructor
   */
  public ManagementPostMenuOrderMocker() {
    menuMappings = new HashMap<>();
  }

  /**
   * Mocks default (0) menu orders for given post ids
   * 
   * @param ids post ids
   * @return self
   */
  public ManagementPostMenuOrderMocker mockMenuOrders(Integer... ids) {
    for (Integer id : ids) {
      mockMenuOrder(id, 0);
    }
    
    return this;
  }
  
  /**
   * Mocks given menu order for the post id
   * 
   * @param id post id 
   * @param menuOrder menu order
   * @return self
   */
  public ManagementPostMenuOrderMocker mockMenuOrder(Integer id, Integer menuOrder) {
    if (isMocking() && menuMappings.containsKey(id)) {
      removeStub(menuMappings.get(id)); 
    }
    
    try {
      menuMappings.put(id, createOkGetMapping(id, menuOrder));
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    if (isMocking()) {
      stubFor(menuMappings.get(id));
    }
    
    return this;
  }
  
  private MappingBuilder createOkGetMapping(Integer id, Integer menuOrder) throws JsonProcessingException {
    UrlPattern urlPattern = urlPathEqualTo(String.format(PATH, id));
    PostMenuOrder resource = new PostMenuOrder();
    resource.setMenuOrder(menuOrder);
    
    return get(urlPattern)
      .willReturn(createOkGetReturn(resource));
  }
  
  private ResponseDefinitionBuilder createOkGetReturn(Object resource) throws JsonProcessingException {
    return aResponse()
      .withHeader(CONTENT_TYPE, APPLICATION_JSON)
      .withBody(toJSON(resource));
  }

}
