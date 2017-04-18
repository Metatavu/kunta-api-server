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

import fi.metatavu.ptv.client.model.V4VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPageChannel;

@SuppressWarnings ({"squid:S1075", "squid:S1450"})
public class PtvServiceChannelMocker extends AbstractPtvMocker<Object> {

  private static final String FAILED_TO_READ_MOCK_FILE = "Failed to read mock file";
  private static final String BASE_PATH = "/ptv/api/v4/ServiceChannel";

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
    if (entity instanceof V4VmOpenApiElectronicChannel) {
      return ((V4VmOpenApiElectronicChannel) entity).getId();
    }
    
    if (entity instanceof V4VmOpenApiPhoneChannel) {
      return ((V4VmOpenApiPhoneChannel) entity).getId();
    }
    
    if (entity instanceof V4VmOpenApiPrintableFormChannel) {
      return ((V4VmOpenApiPrintableFormChannel) entity).getId();
    }
    
    if (entity instanceof V4VmOpenApiServiceLocationChannel) {
      return ((V4VmOpenApiServiceLocationChannel) entity).getId();
    }
    
    if (entity instanceof V4VmOpenApiWebPageChannel) {
      return ((V4VmOpenApiWebPageChannel) entity).getId();
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
