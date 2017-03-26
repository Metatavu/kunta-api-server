package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

import fi.metatavu.management.client.model.Pagemappings;

@SuppressWarnings ("squid:S1166")
public class ManagementPageMappingMocker extends AbstractBaseMocker {

  private static final String PATH = "/wp-json/kunta-api/pagemappings";
  
  private List<Pagemappings> mappings = new ArrayList<>();
  private MappingBuilder mapping = null;
  private UrlPattern urlPattern = urlPathEqualTo(PATH);
  
  @Override
  public void startMock() {
    super.startMock();
    applyMappingStub();
  }
  
  @Override
  public void endMock() {
    removeMappingStub();
    super.endMock();
  }
  
  public ManagementPageMappingMocker removeMapping(String pagePath) {
    removeMappingStub();

    CollectionUtils.filter(mappings, new Predicate<Pagemappings>() {
      @Override
      public boolean evaluate(Pagemappings pagemapping) {
        return StringUtils.equals(pagePath, pagemapping.getPagePath());
      }
    });

    applyMappingStub();

    return this;
  }
  
  public ManagementPageMappingMocker addMapping(String pagePath, String parentPath) {
    removeMappingStub();

    Pagemappings pagemapping = new Pagemappings();
    pagemapping.setPagePath(pagePath);
    pagemapping.setParentPath(parentPath);
    mappings.add(pagemapping);
    
    applyMappingStub();
    
    return this;
  }

  private void applyMappingStub() {
    try {
      mapping = get(urlPattern).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(toJSON(mappings)));
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    stubFor(mapping);
  }

  private void removeMappingStub() {
    removeStub(mapping);
  }
  
}
