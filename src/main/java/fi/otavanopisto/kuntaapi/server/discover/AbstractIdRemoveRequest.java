package fi.otavanopisto.kuntaapi.server.discover;

public abstract class AbstractIdRemoveRequest <T> {

  private T id;  
  
  public AbstractIdRemoveRequest(T id) {
    this.id = id;
  }
  
  public T getId() {
    return id;
  }
  
}
