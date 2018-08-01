package fi.metatavu.kuntaapi.server.index;

public class IndexRequest {

  private Indexable indexable;
  
  public IndexRequest(Indexable indexable) {
    this.indexable = indexable;
  }
  
  public Indexable getIndexable() {
    return indexable;
  }
  
}
