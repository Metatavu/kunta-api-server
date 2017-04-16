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
import fi.metatavu.ptv.client.model.V4VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiService;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPageChannel;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvClient;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelResolver {

  @Inject
  private Logger logger;

  @Inject
  private PtvClient ptvClient;
  
  public V4VmOpenApiElectronicChannel findElectronicChannel(String id) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(id);
    if (serviceChannelData != null) {
      ServiceChannelType type = resolveServiceChannelType(serviceChannelData);
      if (type == ServiceChannelType.ELECTRONIC_CHANNEL) {
        return unserializeElectronicChannel(serializeChannelData(serviceChannelData));
      }
    }
    
    return null;
  }
  
  public V4VmOpenApiServiceLocationChannel findServiceLocationChannel(String id) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(id);
    if (serviceChannelData != null) {
      ServiceChannelType type = resolveServiceChannelType(serviceChannelData);
      if (type == ServiceChannelType.SERVICE_LOCATION) {
        return unserializeServiceLocationChannel(serializeChannelData(serviceChannelData));
      }
    }
    
    return null;
  }
  
  public V4VmOpenApiPrintableFormChannel findPrintableFormChannel(String id) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(id);
    if (serviceChannelData != null) {
      ServiceChannelType type = resolveServiceChannelType(serviceChannelData);
      if (type == ServiceChannelType.PRINTABLE_FORM) {
        return unserializePrintableFormChannel(serializeChannelData(serviceChannelData));
      }
    }
    
    return null;
  }
  
  public V4VmOpenApiPhoneChannel findPhoneChannel(String id) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(id);
    if (serviceChannelData != null) {
      ServiceChannelType type = resolveServiceChannelType(serviceChannelData);
      if (type == ServiceChannelType.PHONE) {
        return unserializePhoneChannel(serializeChannelData(serviceChannelData));
      }
    }
    
    return null;
  }
  
  public V4VmOpenApiWebPageChannel findWebPageChannel(String id) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(id);
    if (serviceChannelData != null) {
      ServiceChannelType type = resolveServiceChannelType(serviceChannelData);
      if (type == ServiceChannelType.WEB_PAGE) {
        return unserializeWebPageChannel(serializeChannelData(serviceChannelData));
      }
    }
    
    return null;
  }
  
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
  
  public V4VmOpenApiElectronicChannel unserializeElectronicChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();
    try {
      return objectMapper.readValue(channelData, V4VmOpenApiElectronicChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize electronic service channel", e);
    }
    
    return null;
  }

  public V4VmOpenApiServiceLocationChannel unserializeServiceLocationChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();
    try {
      return objectMapper.readValue(channelData, V4VmOpenApiServiceLocationChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize service location channel", e);
    }
    
    return null;
  }
  
  public V4VmOpenApiPrintableFormChannel unserializePrintableFormChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V4VmOpenApiPrintableFormChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize printable form service channel", e);
    }
    
    return null;
  }
  
  public V4VmOpenApiPhoneChannel unserializePhoneChannel(byte[] channelData) {
    if (channelData == null) {
      return null;
    }
    
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V4VmOpenApiPhoneChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize phone service channel", e);
    }
    
    return null;
  }
  
  public V4VmOpenApiWebPageChannel unserializeWebPageChannel(byte[] channelData) {
    ObjectMapper objectMapper = createObjectMapper();

    try {
      return objectMapper.readValue(channelData, V4VmOpenApiWebPageChannel.class);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to unserialize web page service channel", e);
    }
    
    return null;
  }
  
  public Map<String, Object> loadServiceChannelData(String serviceChannelId) {
    String path = String.format("/api/v4/ServiceChannel/%s", serviceChannelId);
    ApiResponse<Map<String, Object>> response = ptvClient.doGETRequest(path, new ResultType<Map<String, Object>>() {}, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.log(Level.WARNING, () -> String.format("Service channel %s loading failed on [%d] %s. Request path was %s", serviceChannelId, response.getStatus(), response.getMessage(), path));
    }
    
    return null;
  }
  
  public ServiceChannelType resolveServiceChannelType(Map<String, Object> serviceChannelData) {
    String id = (String) serviceChannelData.get("id");
    
    Object type = serviceChannelData.get("serviceChannelType");
    if (!(type instanceof String)) {
      logger.log(Level.WARNING, () -> String.format("ServiceChannel %s does not have a type", id));
      return null;
    }
    
    return resolveServiceChannelType(id, (String) type);
  }
  
  public ServiceChannelType resolveServiceChannelType(String serviceChannelId) {
    Map<String, Object> serviceChannelData = loadServiceChannelData(serviceChannelId);
    if (serviceChannelData != null) {
      return resolveServiceChannelType(serviceChannelData);
    }
    
    return null;
  }

  public ServiceChannelIds resolveServiceChannelIds(V4VmOpenApiService ptvService) {
    ServiceChannelIds channelIds = new ServiceChannelIds();

    for (V4VmOpenApiServiceServiceChannel ptvChannel : ptvService.getServiceChannels()) {
      ServiceChannelType serviceChannelType = resolveServiceChannelType(ptvChannel.getServiceChannelId());
      if (serviceChannelType != null) {
        switch (serviceChannelType) {
        case ELECTRONIC_CHANNEL:
          channelIds.getElectricChannels().add(ptvChannel.getServiceChannelId());
          break;
        case SERVICE_LOCATION:
          channelIds.getLocationServiceChannels().add(ptvChannel.getServiceChannelId());
          break;
        case PRINTABLE_FORM:
          channelIds.getPrintableFormChannels().add(ptvChannel.getServiceChannelId());
          break;
        case PHONE:
          channelIds.getPhoneChannels().add(ptvChannel.getServiceChannelId());
          break;
        case WEB_PAGE:
          channelIds.getWebPageChannels().add(ptvChannel.getServiceChannelId());
          break;
        default:
          logger.log(Level.SEVERE, () -> String.format("Unknown service channel type %s", serviceChannelType));
          break;
        }
      }
    }

    return channelIds;
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