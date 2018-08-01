package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing menu id
 * 
 * @author Antti Leppä
 */
public class MenuId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 7527026541429916297L;

  /**
   * Zero-argument constructor for menu id
   */
  public MenuId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public MenuId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.MENU;
  }
  
  @Override
  public int getHashInitial() {
    return 143;
  }
  
  @Override
  public int getHashMultiplier() {
    return 155;
  }

}
