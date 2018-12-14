package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

/**
 * Task for PTV service list updates
 * 
 * @author Antti Leppä
 */
public class PtvServiceListTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = 4289945359149088299L;
  
  private Integer page;
  
  /**
   * Constructor
   */
  public PtvServiceListTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param priority whether task is a priority one
   * @param page page index
   */
  public PtvServiceListTask(boolean priority, Integer page) {
	  super(String.format("ptv-service-list-%d", page), priority);
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
