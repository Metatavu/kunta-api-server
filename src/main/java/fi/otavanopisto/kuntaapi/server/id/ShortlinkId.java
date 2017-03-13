package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing shortlink id
 * 
 * @author Antti Leppä
 */
public class ShortlinkId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 8168829851260274148L;

  /**
   * Zero-argument constructor for shortlink id
   */
  public ShortlinkId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ShortlinkId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SHORTLINK;
  }
  
  @Override
  public int getHashInitial() {
    return 247;
  }
  
  @Override
  public int getHashMultiplier() {
    return 259;
  }
  
}
