package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing service location channel id
 * 
 * @author Antti Leppä
 */
public class ServiceLocationChannelId extends BaseId {

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
  protected int getHashInitial() {
    return 211;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 223;
  }
  
}
