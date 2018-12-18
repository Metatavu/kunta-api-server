package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

/**
 * Task for PTV service list updates
 * 
 * @author Antti Leppä
 */
public abstract class AbstractPtvListTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = -677232722559631144L;
  
  private Integer page;
  
  /**
   * Constructor
   */
  public AbstractPtvListTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param priority whether task is a priority one
   * @param page page index
   */
  public AbstractPtvListTask(String entityName, boolean priority, Integer page) {
    super(String.format("ptv-%s-list-%d", entityName, page), priority);
    this.page = page;
  }
  
  /**
   * Returns page index
   * 
   * @return page index
   */
  public Integer getPage() {
    return page;
  }
  
}
