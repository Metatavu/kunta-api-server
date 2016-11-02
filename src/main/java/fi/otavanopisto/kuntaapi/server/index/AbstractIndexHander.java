package fi.otavanopisto.kuntaapi.server.index;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractIndexHander {
  
  @Inject
  private Logger logger;

  @SuppressWarnings("resource")
  protected TransportClient createClient() {
    TransportClient transportClient = null;
    
    try {
      Settings settings = Settings.builder()
        .build();
      
      transportClient = new PreBuiltTransportClient(settings)
          .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
      
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
