package fi.otavanopisto.kuntaapi.server.index;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractIndexHander {
  
  @Inject
  private Logger logger;

  protected TransportClient createClient(String clusterName, String[] hosts) {
    TransportClient transportClient = null;
    
    try {
      Settings settings = Settings.builder()
        .put("cluster.name", clusterName)
        .build();
      
      transportClient = new PreBuiltTransportClient(settings);
      
      for (String host : hosts) {
        String[] parts = StringUtils.split(host, ':');
        if (parts.length != 2 || !NumberUtils.isNumber(parts[1])) {
          logger.severe(String.format("Invalid elastic search host %s, dropped", host));
        }
        
        String name = parts[0];
        Integer port = NumberUtils.createInteger(parts[1]);
        transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(name), port));
      }
      
    } catch (UnknownHostException e) {
      logger.log(Level.SEVERE, "Could not connect to elastic search cluster", e);
      return null;
    }

    prepareIndex(transportClient);
    
    return transportClient;
  }
  
  protected void closeClient(TransportClient transportClient) {
    transportClient.close();
  }
  
  protected byte[] serialize(Indexable indexable) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsBytes(indexable);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize indexable object", e);
    }
    
    return new byte[0];
  }
  
  public abstract String getIndex();
  
  private void prepareIndex(TransportClient transportClient) {
    if (!indexExists(transportClient)) {
      createIndex(transportClient);
    }
  }
  
  private boolean indexExists(TransportClient transportClient) {
    return transportClient
      .admin()
      .indices()
      .prepareExists(getIndex())
      .execute()
      .actionGet()
      .isExists();
  }

  private void createIndex(TransportClient transportClient) {
    transportClient
      .admin()
      .indices()
      .prepareCreate(getIndex())
      .execute()
      .actionGet();
  }
}
