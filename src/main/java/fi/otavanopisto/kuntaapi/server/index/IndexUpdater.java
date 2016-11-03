package fi.otavanopisto.kuntaapi.server.index;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Singleton
public class IndexUpdater extends AbstractIndexHander {
 
  @Lock (LockType.READ)
  public void index(Indexable indexable) {
    getClient().prepareIndex(getIndex(), indexable.getType(), indexable.getId())
      .setSource(serialize(indexable))
      .execute()
      .actionGet();
  }

}