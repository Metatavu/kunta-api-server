package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing fragment id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportAgencyId extends OrganizationBaseId {

  private static final long serialVersionUID = 590429357437798415L;

  /**
   * Zero-argument constructor for public transport agency id
   */
  public PublicTransportAgencyId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PublicTransportAgencyId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_AGENCY;
  }
  
  @Override
  protected int getHashInitial() {
    return 337;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 349;
  }
  
}
