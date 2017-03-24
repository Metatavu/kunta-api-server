package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing phone service channel id
 * 
 * @author Antti Leppä
 */
public class PhoneServiceChannelId extends BaseId {

  private static final long serialVersionUID = -6439252801891529427L;

  /**
   * Zero-argument constructor for phone service channel id
   */
  public PhoneServiceChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PhoneServiceChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PHONE_SERVICE_CHANNEL;
  }
  
  @Override
  public int getHashInitial() {
    return 171;
  }
  
  @Override
  public int getHashMultiplier() {
    return 183;
  }
  
}
