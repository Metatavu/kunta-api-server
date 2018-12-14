package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

/**
 * Task for PTV service channel list updates
 * 
 * @author Antti Leppä
 */
public class PtvServiceChannelListTask extends AbstractPtvListTask {
  
  private static final long serialVersionUID = 6289836109596471295L;

  /**
   * Constructor
   */
  public PtvServiceChannelListTask() {
    super();
  }
  
  /**
   * Constructor
   * 
   * @param priority whether task is a priority one
   * @param page page index
   */
  public PtvServiceChannelListTask(boolean priority, Integer page) {
	  super("servicechannel", priority, page);
  }
  
}
