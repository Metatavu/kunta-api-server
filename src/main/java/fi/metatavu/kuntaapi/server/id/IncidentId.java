package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing incident id
 * 
 * @author Antti Leppä
 */
public class IncidentId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -8877125407405908458L;

  /**
   * Zero-argument constructor for incident id
   */
  public IncidentId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public IncidentId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.INCIDENT;
  }
  
  @Override
  public int getHashInitial() {
    return 249;
  }
  
  @Override
  public int getHashMultiplier() {
    return 261;
  }
  
}
