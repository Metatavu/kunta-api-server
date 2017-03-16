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
  private BaseId parentId;
  
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
   * @param parentId parent id
   * @param id id
   * @param orderIndex order index
   */
  public IdTask(Operation operation, BaseId parentId, I id, Long orderIndex) {
    this.parentId = parentId;
    this.id = id;
    this.operation = operation;
    this.orderIndex = orderIndex;
  }
  
  /**
   * Constructor for id task
   * 
   * @param operation operation
   * @param id id
   * @param orderIndex order index
   */
  public IdTask(Operation operation, I id, Long orderIndex) {
    this(operation, null, id, orderIndex);
  }
  
  /**
   * Constructor for id task
   * 
   * @param operation operation
   * @param id id
   */
  public IdTask(Operation operation, I id) {
    this(operation, id, null);
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
   * Returns parent id or null if not set
   * 
   * @return parent id or null if not set
   */
  public BaseId getParentId() {
    return parentId;
  }
  
  /**
   * Sets a parent id
   * 
   * @param parentId a parent id
   */
  public void setParentId(BaseId parentId) {
    this.parentId = parentId;
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
