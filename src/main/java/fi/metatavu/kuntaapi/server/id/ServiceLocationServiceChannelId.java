package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing service location channel id
 * 
 * @author Antti Leppä
 */
public class ServiceLocationServiceChannelId extends BaseId {

  private static final long serialVersionUID = 3064775100960250396L;

  /**
   * Zero-argument constructor for service location channel id
   */
  public ServiceLocationServiceChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ServiceLocationServiceChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE_LOCATION_SERVICE_CHANNEL;
  }
  
  @Override
  public int getHashInitial() {
    return 211;
  }
  
  @Override
  public int getHashMultiplier() {
    return 223;
  }
  
}
