package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import java.io.Serializable;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public abstract class AbstractGtfsEntityTask <E extends Serializable> extends AbstractTask {

  private static final long serialVersionUID = 2631366388691557632L;
  
  private E entity;  
  private Long orderIndex;
  
  public AbstractGtfsEntityTask() {
    // Zero-argument constructor
  }
  
  public AbstractGtfsEntityTask(E entity, Long orderIndex) {
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
