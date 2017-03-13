package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing electronic service channel id
 * 
 * @author Antti Leppä
 */
public class ElectronicServiceChannelId extends BaseId {

  private static final long serialVersionUID = 3328936895279347939L;

  /**
   * Zero-argument constructor for electronic service channel id
   */
  public ElectronicServiceChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ElectronicServiceChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ELECTRONIC_SERVICE_CHANNEL;
  }
  
  @Override
  public int getHashInitial() {
    return 151;
  }
  
  @Override
  public int getHashMultiplier() {
    return 163;
  }
  
}
