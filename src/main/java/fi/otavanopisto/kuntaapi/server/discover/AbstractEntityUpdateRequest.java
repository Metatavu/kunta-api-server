package fi.otavanopisto.kuntaapi.server.discover;

public abstract class AbstractEntityUpdateRequest <E> {

  private E entity;  
  private boolean priority;
  private Long orderIndex;
  
  public AbstractEntityUpdateRequest(E entity, Long orderIndex, boolean priority) {
    this.entity = entity;
    this.orderIndex = orderIndex;
    this.priority = priority;
  }
  
  public E getEntity() {
    return entity;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public boolean isPriority() {
    return priority;
  }
  
}
