package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing banner id
 * 
 * @author Antti Leppä
 */
public class BannerId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 2132259406536383457L;

  /**
   * Zero-argument constructor for banner id
   */
  public BannerId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public BannerId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.BANNER;
  }
  
  @Override
  public int getHashInitial() {
    return 137;
  }
  
  @Override
  public int getHashMultiplier() {
    return 149;
  }
  
}
