package fi.metatavu.kuntaapi.server.index;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

@ApplicationScoped
public class Indexer {
  
  @Inject
  private IndexUpdater indexUpdater;
  
  @Transactional (TxType.REQUIRES_NEW)
  public void onIndexRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) IndexRequest indexRequest) {
    Indexable indexable = indexRequest.getIndexable();
    indexUpdater.index(indexable);
  }
  
  @Transactional (TxType.REQUIRES_NEW)
  public void onIndexRemoveRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) IndexRemoveRequest indexRemoveRequest) {
    IndexRemove indexRemove = indexRemoveRequest.getIndexRemove();
    indexUpdater.remove(indexRemove);
  }
}
