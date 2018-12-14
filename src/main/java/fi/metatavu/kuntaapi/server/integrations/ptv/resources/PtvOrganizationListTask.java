package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

/**
 * Task for PTV organization list updates
 * 
 * @author Antti Leppä
 */
public class PtvOrganizationListTask extends AbstractPtvListTask {
  
  private static final long serialVersionUID = 3235701470480843977L;

  /**
   * Constructor
   */
  public PtvOrganizationListTask() {
    super();
  }
  
  /**
   * Constructor
   * 
   * @param priority whether task is a priority one
   * @param page page index
   */
  public PtvOrganizationListTask(boolean priority, Integer page) {
	  super("organization", priority, page);
  }
  
}
