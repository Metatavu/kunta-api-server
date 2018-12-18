package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import java.io.Serializable;

import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

public abstract class AbstractGtfsEntityTask <E extends Serializable> extends DefaultTaskImpl {

  private static final long serialVersionUID = 2631366388691557632L;
  
  private E entity;  
  private Long orderIndex;
  
  public AbstractGtfsEntityTask() {
    // Zero-argument constructor
  }
  
  public AbstractGtfsEntityTask(String uniqueId, boolean priority, E entity, Long orderIndex) {
    super(uniqueId, priority);
    this.entity = entity;
    this.orderIndex = orderIndex;
  }
  
  public E getEntity() {
    return entity;
  }
  
  public void setEntity(E entity) {
    this.entity = entity;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
}
