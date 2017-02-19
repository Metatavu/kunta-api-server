package fi.otavanopisto.kuntaapi.server.tasks;

import fi.otavanopisto.kuntaapi.server.id.BaseId;

/**
 * Id task
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public class IdTask <I extends BaseId> extends AbstractTask {
  
  private static final long serialVersionUID = -9061073739316230871L;
  
  private I id;
  private Long orderIndex;
  private Operation operation;
  
  /**
   * Zero-argument constructor of id taks
   */
  public IdTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor for id task
   * 
   * @param operation operation
   * @param id id
   * @param orderIndex order index
   */
  public IdTask(Operation operation, I id, Long orderIndex) {
    this.id = id;
    this.operation = operation;
    this.orderIndex = orderIndex;
  }
  
  /**
   * Returns id
   * 
   * @return id
   */
  public I getId() {
    return id;
  }
  
  /**
   * Sets id
   * 
   * @param id id
   */
  public void setId(I id) {
    this.id = id;
  }
  
  /**
   * Returns operation
   * 
   * @return operation
   */
  public Operation getOperation() {
    return operation;
  }
  
  /**
   * Sets operation
   * 
   * @param operation operation
   */
  public void setOperation(Operation operation) {
    this.operation = operation;
  }
  
  /**
   * Returns order index
   * 
   * @return order index
   */
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  /**
   * Sets an order index
   * 
   * @param orderIndex order index
   */
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  /**
   * Enumeration that describes an operation
   * 
   * @author Antti Leppä
   */
  public enum Operation {
    
    UPDATE,
    
    REMOVE
    
  }
  
}
