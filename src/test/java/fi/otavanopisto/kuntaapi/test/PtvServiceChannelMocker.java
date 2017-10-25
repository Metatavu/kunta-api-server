package fi.otavanopisto.kuntaapi.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.ptv.client.model.V7VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannel;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.ptv.client.model.V7VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiWebPageChannel;

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
  protected Object readEntity(String id) {
    return readEntityFromJSONFile(String.format("ptv/%s/%s.json", getName(), id));
  }

  @SuppressWarnings("unchecked")
  private Object readEntityFromJSONFile(String file) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return objectMapper.readValue(stream, new TypeReference<Map<Object, Object>>() { });
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_TO_READ_MOCK_FILE, e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  
}
