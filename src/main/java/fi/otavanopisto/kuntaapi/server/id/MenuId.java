package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing menu id
 * 
 * @author Antti Leppä
 */
public class MenuId extends OrganizationBaseId {
  
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
  protected int getHashInitial() {
    return 143;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 155;
  }

}
