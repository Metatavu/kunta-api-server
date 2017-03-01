package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

public class AbstractManagementMocker extends AbstractMocker {

  private boolean mocking = false;

  @Override
  public void startMock() {
    mocking = true;
  }
  
  @Override
  public void endMock() {
    mocking = false;
  }
  
  public boolean isMocking() {
    return mocking;
  }

  protected MappingBuilder createNotFoundMapping(String path) {
    MappingBuilder mappingBuilder = get(urlPathEqualTo(path));
    
    return mappingBuilder
      .willReturn(aResponse()
      .withStatus(404));
  }
  
}
