package fi.metatavu.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;

@SuppressWarnings ({"squid:S1075", "squid:S1450"})
public class PtvServiceChannelMocker extends AbstractPtvMocker<Object> {

  private static final String FAILED_TO_READ_MOCK_FILE = "Failed to read mock file";
  private static final String BASE_PATH = String.format("/ptv/api/%s/ServiceChannel", PtvConsts.VERSION);

  private static Logger logger = Logger.getLogger(PtvServiceChannelMocker.class.getName());

  @Override
  public String getName() {
    return "servicechannels";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(Object entity) {
    if (entity instanceof V9VmOpenApiElectronicChannel) {
      return ((V9VmOpenApiElectronicChannel) entity).getId().toString();
    }
    
    if (entity instanceof V9VmOpenApiPhoneChannel) {
      return ((V9VmOpenApiPhoneChannel) entity).getId().toString();
    }
    
    if (entity instanceof V9VmOpenApiPrintableFormChannel) {
      return ((V9VmOpenApiPrintableFormChannel) entity).getId().toString();
    }
    
    if (entity instanceof V9VmOpenApiServiceLocationChannel) {
      return ((V9VmOpenApiServiceLocationChannel) entity).getId().toString();
    }
    
    if (entity instanceof V9VmOpenApiWebPageChannel) {
      return ((V9VmOpenApiWebPageChannel) entity).getId().toString();
    }
    
    return null;
  }

  @Override
  public Object readEntity(String api, String id) {
    return readEntityFromJSONFile(String.format("ptv/%s/%s/%s.json", api, getName(), id));
  }
  
  public void mockElectronicPut(String id, V9VmOpenApiElectronicChannel responseEntity) {
    mockServiceChannelPut("EChannel", id, responseEntity);
  }
  
  public void mockPhonePut(String id, V9VmOpenApiPhoneChannel responseEntity) {
    mockServiceChannelPut("Phone", id, responseEntity);
  }
  
  public void mockPrintableFormPut(String id, V9VmOpenApiPrintableFormChannel responseEntity) {
    mockServiceChannelPut("PrintableForm", id, responseEntity);
  }
  
  public void mockServiceLocationPut(String id, V9VmOpenApiServiceLocationChannel responseEntity) {
    mockServiceChannelPut("ServiceLocation", id, responseEntity);
  }

  public void mockWebpagePut(String id, V9VmOpenApiWebPageChannel responseEntity) {
    mockServiceChannelPut("WebPage", id, responseEntity);
  }
  
  public void verifyElectronic(String id, V9VmOpenApiElectronicChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/EChannel/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyPhone(String id, V9VmOpenApiPhoneChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/Phone/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyPrintableForm(String id, V9VmOpenApiPrintableFormChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/PrintableForm/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyWebPage(String id, V9VmOpenApiWebPageChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/WebPage/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyServiceLocation(String id, V9VmOpenApiServiceLocationChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/ServiceLocation/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  private void mockServiceChannelPut(String type, String id, Object responseEntity) {
    stubFor(put(urlEqualTo(String.format("/ptv/api/%s/ServiceChannel/%s/%s", PtvConsts.VERSION, type, id)))
        .willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(toJSON(responseEntity)))); 
  }
  
  private Object readEntityFromJSONFile(String file) {
    ObjectMapper objectMapper = getObjectMapper();
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return objectMapper.readValue(stream, new TypeReference<Map<Object, Object>>() { });
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  
}
