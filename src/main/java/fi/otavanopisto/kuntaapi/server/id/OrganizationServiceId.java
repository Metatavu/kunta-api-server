package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing service id
 * 
 * @author Antti Leppä
 */
public class OrganizationServiceId extends OrganizationBaseId {

  /**
   * Zero-argument constructor for organization service id
   */
  public OrganizationServiceId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public OrganizationServiceId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ORGANIZATION_SERVICE;
  }
  
  @Override
  protected int getHashInitial() {
    return 149;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 161;
  }
  
}
