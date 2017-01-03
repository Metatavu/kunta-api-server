package fi.otavanopisto.kuntaapi.server.discover;

public abstract class AbstractIdUpdateRequest <T> {

  private T id;  
  private boolean priority;
  private Long orderIndex;
  
  public AbstractIdUpdateRequest(T id, Long orderIndex, boolean priority) {
    this.id = id;
    this.orderIndex = orderIndex;
    this.priority = priority;
  }
  
  public T getId() {
    return id;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public boolean isPriority() {
    return priority;
  }
  
}
