package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing page id
 * 
 * @author Antti Leppä
 */
public class PageId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 5114032971461208167L;

  /**
   * Zero-argument constructor for page id
   */
  public PageId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PageId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PAGE;
  }
  
  @Override
  public int getHashInitial() {
    return 147;
  }
  
  @Override
  public int getHashMultiplier() {
    return 159;
  }
  
}
