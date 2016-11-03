package fi.otavanopisto.kuntaapi.server.index;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.client.transport.TransportClient;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
public class IndexUpdater extends AbstractIndexHander {
 
  private static final String DEFAULT_INDEX = "kunta-api";
  private static final String DEFAULT_CLUSTERNAME = "elasticsearch";
  private static final String[] DEFAULT_HOSTS = new String[] {
    "localhost:9300"
  };

  @Inject
  private SystemSettingController systemSettingController;
  
  private String index;
  private TransportClient client;
  
  @PostConstruct
  public void init() {
    String[] hosts = systemSettingController.getSettingValues(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_SEARCH_HOSTS, DEFAULT_HOSTS);
    String clusterName = systemSettingController.getSettingValue(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_CLUSTER_NAME, DEFAULT_CLUSTERNAME);
    index = systemSettingController.getSettingValue(KuntaApiConsts.SYSTEM_SETTING_ELASTIC_INDEX, DEFAULT_INDEX);
    
    client = createClient(clusterName, hosts);
  }
  
  @PreDestroy
  public void deinit() {
    closeClient(client);
  }
  
  @Asynchronous
  @Lock (LockType.READ)
  public void index(Indexable indexable) {
    client.prepareIndex(getIndex(), indexable.getType(), indexable.getId())
      .setSource(serialize(indexable))
      .execute()
      .actionGet();
  }

  @Override
  public String getIndex() {
    return index;
  }

}