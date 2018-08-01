package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing emergency id
 * 
 * @author Antti Leppä
 */
public class EmergencyId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -598715927944729805L;

  /**
   * Zero-argument constructor for emergency id
   */
  public EmergencyId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public EmergencyId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.EMERGENCY;
  }
  
  @Override
  public int getHashInitial() {
    return 251;
  }
  
  @Override
  public int getHashMultiplier() {
    return 263;
  }
  
}
