package fi.metatavu.kuntaapi.server.index;

public class IndexRemoveRequest {

  private IndexRemove indexRemove;
  
  public IndexRemoveRequest(IndexRemove indexRemove) {
    this.indexRemove = indexRemove;
  }
  
  public IndexRemove getIndexRemove() {
    return indexRemove;
  }
  
  public void setIndexRemove(IndexRemove indexRemove) {
    this.indexRemove = indexRemove;
  }
  
}
