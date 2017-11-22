package fi.otavanopisto.kuntaapi.server.integrations.ptv.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.otavanopisto.kuntaapi.server.integrations.AbstractHttpClient;

public class PtvHttpClient extends AbstractHttpClient {

  @Override
  protected ObjectMapper getJsonObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    registerModules(objectMapper);
    return objectMapper;
  }
  
}
