package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing environmental warning id
 * 
 * @author Antti Leppä
 */
public class EnvironmentalWarningId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -1583566805362578411L;

  /**
   * Zero-argument constructor for event id
   */
  public EnvironmentalWarningId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public EnvironmentalWarningId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ENVIRONMENTAL_WARNING;
  }
  
  @Override
  public int getHashInitial() {
    return 523;
  }
  
  @Override
  public int getHashMultiplier() {
    return 535;
  }
  
}
