package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

/**
 * Task for PTV service list updates
 * 
 * @author Antti Leppä
 */
public class PtvServiceListTask extends AbstractPtvListTask {
  
  private static final long serialVersionUID = 4897379861552685138L;

  /**
   * Constructor
   */
  public PtvServiceListTask() {
    super();
  }
  
  /**
   * Constructor
   * 
   * @param priority whether task is a priority one
   * @param page page index
   */
  public PtvServiceListTask(boolean priority, Integer page) {
    super("service", priority, page);
  }
  
}
