package fi.otavanopisto.kuntaapi.server.discover;

public abstract class AbstractIdUpdateRequest <T> {

  private T id;  
  private boolean priority;
  
  public AbstractIdUpdateRequest(T id, boolean priority) {
    this.id = id;
    this.priority = priority;
  }
  
  public T getId() {
    return id;
  }
  
  public boolean isPriority() {
    return priority;
  }
  
}
