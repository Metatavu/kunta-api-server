package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing contact id
 * 
 * @author Antti Leppä
 */
public class ContactId extends OrganizationBaseId {

  private static final long serialVersionUID = 6959970320549914044L;

  /**
   * Zero-argument constructor for contact id
   */
  public ContactId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ContactId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.CONTACT;
  }
  
  @Override
  public int getHashInitial() {
    return 333;
  }
  
  @Override
  public int getHashMultiplier() {
    return 345;
  }
  
}
