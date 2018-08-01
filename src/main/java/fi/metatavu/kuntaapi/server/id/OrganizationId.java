package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing organization id
 * 
 * @author Antti Leppä
 */
public class OrganizationId extends BaseId {
  
  private static final long serialVersionUID = 3378366537573061902L;

  /**
   * Zero-argument constructor for organization id
   */
  public OrganizationId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public OrganizationId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ORGANIZATION;
  }
  
  @Override
  public int getHashInitial() {
    return 127;
  }
  
  @Override
  public int getHashMultiplier() {
    return 139;
  }
  
}
