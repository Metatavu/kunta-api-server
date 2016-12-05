package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing menu item id
 * 
 * @author Antti Leppä
 */
public class MenuItemId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 3712558754323736611L;

  /**
   * Zero-argument constructor for menu item id
   */
  public MenuItemId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public MenuItemId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.MENU_ITEM;
  }
  
  @Override
  protected int getHashInitial() {
    return 145;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 157;
  }
  
}
