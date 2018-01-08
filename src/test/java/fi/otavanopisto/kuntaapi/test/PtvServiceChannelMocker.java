package fi.otavanopisto.kuntaapi.test;

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

import fi.metatavu.ptv.client.model.V6VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V6VmOpenApiWebPageChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiWebPageChannel;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

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
    if (entity instanceof V7VmOpenApiElectronicChannel) {
      return ((V7VmOpenApiElectronicChannel) entity).getId().toString();
    }
    
    if (entity instanceof V7VmOpenApiPhoneChannel) {
      return ((V7VmOpenApiPhoneChannel) entity).getId().toString();
    }
    
    if (entity instanceof V7VmOpenApiPrintableFormChannel) {
      return ((V7VmOpenApiPrintableFormChannel) entity).getId().toString();
    }
    
    if (entity instanceof V7VmOpenApiServiceLocationChannel) {
      return ((V7VmOpenApiServiceLocationChannel) entity).getId().toString();
    }
    
    if (entity instanceof V7VmOpenApiWebPageChannel) {
      return ((V7VmOpenApiWebPageChannel) entity).getId().toString();
    }
    
    return null;
  }

  @Override
  public Object readEntity(String api, String id) {
    return readEntityFromJSONFile(String.format("ptv/%s/%s/%s.json", api, getName(), id));
  }
  
  public void mockElectronicPut(String id, V7VmOpenApiElectronicChannel responseEntity) {
    mockServiceChannelPut("EChannel", id, responseEntity);
  }
  
  public void mockPhonePut(String id, V7VmOpenApiPhoneChannel responseEntity) {
    mockServiceChannelPut("Phone", id, responseEntity);
  }
  
  public void mockPrintableFormPut(String id, V7VmOpenApiPrintableFormChannel responseEntity) {
    mockServiceChannelPut("PrintableForm", id, responseEntity);
  }
  
  public void mockServiceLocationPut(String id, V7VmOpenApiServiceLocationChannel responseEntity) {
    mockServiceChannelPut("ServiceLocation", id, responseEntity);
  }

  public void mockWebpagePut(String id, V7VmOpenApiWebPageChannel responseEntity) {
    mockServiceChannelPut("WebPage", id, responseEntity);
  }
  
  public void verifyElectronic(String id, V6VmOpenApiElectronicChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/EChannel/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyPhone(String id, V7VmOpenApiPhoneChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/Phone/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyPrintableForm(String id, V7VmOpenApiPrintableFormChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/PrintableForm/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyWebPage(String id, V6VmOpenApiWebPageChannelInBase entity) {
    verifyPut(String.format("/ptv/api/%s/ServiceChannel/WebPage/%s", PtvConsts.VERSION, id), toJSON(entity));
  }
  
  public void verifyServiceLocation(String id, V7VmOpenApiServiceLocationChannelInBase entity) {
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
