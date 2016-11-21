package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing announcement id
 * 
 * @author Antti Leppä
 */
public class AnnouncementId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 7189168658256791223L;

  /**
   * Zero-argument constructor for announcement id
   */
  public AnnouncementId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public AnnouncementId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ANNOUNCEMENT;
  }
  
  @Override
  protected int getHashInitial() {
    return 235;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 247;
  }
  
}
