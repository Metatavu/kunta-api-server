package fi.otavanopisto.kuntaapi.server.index;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

public abstract class AbstractIndexHander {

  public static final String ORDER_INDEX_FIELD = "orderIndex";
  
  private static final String DEFAULT_INDEX = "kunta-api";
  private static final String DEFAULT_CLUSTERNAME = "elasticsearch";
  private static final String[] DEFAULT_HOSTS = new String[] {
    "localhost:9300"
  };

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private Logger logger;
  
  private String index;
  private TransportClient client;
  
  
  @PostConstruct
  public void init() {
    String[] hosts = systemSettingController.getSettingValues(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_SEARCH_HOSTS, DEFAULT_HOSTS);
    String clusterName = systemSettingController.getSettingValue(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_CLUSTER_NAME, DEFAULT_CLUSTERNAME);
    index = systemSettingController.getSettingValue(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_INDEX, DEFAULT_INDEX);
    client = createTransportClient(hosts, clusterName);
    setup();
  }
  
  @PreDestroy
  public void deinit() {
    if (client != null) {
      closeClient(client);
    }
  }
  
  public boolean isEnabled() {
    return client != null;
  }
  
  public abstract void setup();
  
  protected Client getClient() {
    return client;
  }
  
  protected byte[] serialize(Indexable indexable) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return objectMapper.writeValueAsBytes(indexable);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize indexable object", e);
    }
    
    return new byte[0];
  }
  
  protected String getIndex() {
    return index;
  }
  
  private TransportClient createTransportClient(String[] hosts, String clusterName) {
    try {
      TransportClient transportClient;
      Settings settings = Settings.builder()
        .put("cluster.name", clusterName)
        .build();
      
      transportClient = new PreBuiltTransportClient(settings);
      
      for (String host : hosts) {
        String[] parts = StringUtils.split(host, ':');
        if (parts.length != 2 || !NumberUtils.isNumber(parts[1])) {
          logger.severe(() -> String.format("Invalid elastic search host %s, dropped", host));
        }
        
        String name = parts[0];
        Integer port = NumberUtils.createInteger(parts[1]);
        transportClient.addTransportAddress(resolveTransportAddress(name, port));
      }
  
      prepareIndex(transportClient);

      return transportClient;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Elastic client creation failed. All search functions are disbled", e);
    }

    return null;
  }
  
  private InetSocketTransportAddress resolveTransportAddress(String name, int port) {
    return new InetSocketTransportAddress(resolveInetAddress(name), port);
  }
  
  private InetAddress resolveInetAddress(String name) {
    try {
      return InetAddress.getByName(name);
    } catch (UnknownHostException e) {
      logger.log(Level.SEVERE, String.format("Could resolve address %s, falling back to localhost", name), e);
    }
    
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      logger.log(Level.SEVERE, "Could not resolve localhost either", e);
    }
    
    return null;
  }
  
  private void closeClient(TransportClient transportClient) {
    transportClient.close();
  }
  
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
