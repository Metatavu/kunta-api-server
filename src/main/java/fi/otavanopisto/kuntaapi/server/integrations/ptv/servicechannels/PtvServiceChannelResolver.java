package fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ResultType;
import fi.metatavu.ptv.client.model.V7VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiWebPageChannel;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvClient;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelResolver {

  @Inject
  private Logger logger;

  @Inject
  private PtvClient ptvClient;

  @SuppressWarnings ("squid:S1168")
  public byte[] serializeChannelData(Map<String, Object> serviceChannelData) {
    if (serviceChannelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();
    
    try {
      return objectMapper.writeValueAsBytes(serviceChannelData);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize channel data", e);
    }
    
    return null;
  }
  
  public V7VmOpenApiElectronicChannel unserializeElectronicChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();
    try {
      return objectMapper.readValue(channelData, V7VmOpenApiElectronicChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize electronic service channel", e);
    }
    
    return null;
  }

  public V7VmOpenApiServiceLocationChannel unserializeServiceLocationChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();
    try {
      return objectMapper.readValue(channelData, V7VmOpenApiServiceLocationChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize service location channel", e);
    }
    
    return null;
  }
  
  public V7VmOpenApiPrintableFormChannel unserializePrintableFormChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V7VmOpenApiPrintableFormChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize printable form service channel", e);
    }
    
    return null;
  }
  
  public V7VmOpenApiPhoneChannel unserializePhoneChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V7VmOpenApiPhoneChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize phone service channel", e);
    }
    
    return null;
  }
  
  public V7VmOpenApiWebPageChannel unserializeWebPageChannel(byte[] channelData) {
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V7VmOpenApiWebPageChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize web page service channel", e);
    }
    
    return null;
  }
  
  public Map<String, Object> loadServiceChannelData(String serviceChannelId) {
    String path = String.format("/api/%s/ServiceChannel/%s", PtvConsts.VERSION, serviceChannelId);
    ApiResponse<Map<String, Object>> response = ptvClient.doGETRequest(null, path, new ResultType<Map<String, Object>>() {}, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.log(Level.WARNING, () -> String.format("Service channel %s loading failed on [%d] %s. Request path was %s", serviceChannelId, response.getStatus(), response.getMessage(), path));
    }
    
    return null;
  }
  
  public ServiceChannelType resolveServiceChannelType(Map<String, Object> serviceChannelData) {
    
    if (serviceChannelData == null) {
      logger.log(Level.WARNING, () -> "Null serviceChannelData");
      return null;
    }
    
    String id = (String) serviceChannelData.get("id");
    
    Object type = serviceChannelData.get("serviceChannelType");
    if (!(type instanceof String)) {
      logger.log(Level.WARNING, () -> String.format("ServiceChannel %s does not have a type", id));
      return null;
    }
    
    return resolveServiceChannelType(id, (String) type);
  }

  @SuppressWarnings ("squid:MethodCyclomaticComplexity")
  private ServiceChannelType resolveServiceChannelType(String id, String type) {
    switch (type) {
      case "EChannel":
        return ServiceChannelType.ELECTRONIC_CHANNEL;
      case "ServiceLocation":
        return ServiceChannelType.SERVICE_LOCATION;
      case "PrintableForm":
        return ServiceChannelType.PRINTABLE_FORM;
      case "Phone":
        return ServiceChannelType.PHONE;
      case "WebPage":
        return ServiceChannelType.WEB_PAGE;
      default:
        if (logger.isLoggable(Level.SEVERE)) {
          logger.severe(String.format("ServiceChannel %s has unknown type %s", id, (String) type));
        }
        return null;
    }
  }
  
  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  

}
