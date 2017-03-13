package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing service location channel id
 * 
 * @author Antti Leppä
 */
public class ServiceLocationChannelId extends BaseId {

  private static final long serialVersionUID = 3064775100960250396L;

  /**
   * Zero-argument constructor for service location channel id
   */
  public ServiceLocationChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ServiceLocationChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE_LOCATION_CHANNEL;
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
