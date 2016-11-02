package fi.otavanopisto.kuntaapi.server.index;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

@ApplicationScoped
public class Indexer {
  
  @Inject
  private IndexUpdater indexUpdater;

  @Asynchronous
  public void onIndexRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) IndexRequest indexRequest) {
    Indexable indexable = indexRequest.getIndexable();
    indexUpdater.index(indexable);
  }
  
}
