package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import fi.metatavu.ptv.client.model.V7VmOpenApiService;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceAndChannelRelationInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceInBase;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

@SuppressWarnings ("squid:S1450")
public class PtvServiceMocker extends AbstractPtvMocker<V7VmOpenApiService> {

  private static final String BASE_PATH = String.format("/ptv/api/%s/Service", PtvConsts.VERSION);

  @Override
  public String getName() {
    return "services";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(V7VmOpenApiService entity) {
    return entity.getId().toString();
  }

  public void mockServicePut(String id, V7VmOpenApiService responseEntity) {
    stubFor(put(urlPathEqualTo(String.format("/ptv/api/%s/Service/%s", PtvConsts.VERSION, id)))
      .withQueryParam("attachProposedChannels", equalTo("false"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody(toJSON(responseEntity)))); 
    
    stubFor(put(urlPathEqualTo(String.format("/ptv/api/%s/Connection/serviceId/%s", PtvConsts.VERSION, id)))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody(toJSON(responseEntity)))); 
  }
  
  public void verifyService(String id, V7VmOpenApiServiceInBase entity) {
    String payload = toJSON(entity);
    verify(1, putRequestedFor(urlPathEqualTo(String.format("/ptv/api/%s/Service/%s", PtvConsts.VERSION, id)))
      .withQueryParam("attachProposedChannels", equalTo("false"))
      .withRequestBody(equalToJson(payload, true, true)));
  }

  public void verifyServiceConnection(String id, V7VmOpenApiServiceAndChannelRelationInBase entity) {
    String payload = toJSON(entity);
    
    verify(1, putRequestedFor(urlPathEqualTo(String.format("/ptv/api/%s/Connection/serviceId/%s", PtvConsts.VERSION, id)))
        .withRequestBody(equalToJson(payload, true, true)));
  }
  
}
