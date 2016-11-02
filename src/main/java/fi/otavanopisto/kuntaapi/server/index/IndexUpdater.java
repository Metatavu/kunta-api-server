package fi.otavanopisto.kuntaapi.server.index;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.elasticsearch.client.transport.TransportClient;

@ApplicationScoped
public class IndexUpdater extends AbstractIndexHander {
 
  private TransportClient client;
  
  @PostConstruct
  public void init() {
    client = createClient();
  }
  
  @PreDestroy
  public void deinit() {
    closeClient(client);
  }
  
  public void index(Indexable indexable) {
    client.prepareIndex(getIndex(), indexable.getType(), indexable.getId())
      .setSource(serialize(indexable))
      .execute()
      .actionGet();
  }

  @Override
  public String getIndex() {
    return "develop";
  }

}