package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing fragment id
 * 
 * @author Antti Leppä
 */
public class FragmentId extends OrganizationBaseId {

  private static final long serialVersionUID = 4440603743466606731L;

  /**
   * Zero-argument constructor for fragment id
   */
  public FragmentId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public FragmentId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.FRAGMENT;
  }
  
  @Override
  public int getHashInitial() {
    return 335;
  }
  
  @Override
  public int getHashMultiplier() {
    return 347;
  }
  
}
