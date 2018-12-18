package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing job id
 * 
 * @author Antti Leppä
 */
public class JobId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 4430136965378549999L;

  /**
   * Zero-argument constructor for job id
   */
  public JobId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public JobId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.JOB;
  }
  
  @Override
  public int getHashInitial() {
    return 233;
  }
  
  @Override
  public int getHashMultiplier() {
    return 245;
  }
  
}
