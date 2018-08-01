package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing service id
 * 
 * @author Antti Leppä
 */
public class ServiceId extends BaseId {

  private static final long serialVersionUID = -3061405537734005057L;

  /**
   * Zero-argument constructor for service id
   */
  public ServiceId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ServiceId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE;
  }
  
  @Override
  public int getHashInitial() {
    return 133;
  }
  
  @Override
  public int getHashMultiplier() {
    return 145;
  }
  
}
