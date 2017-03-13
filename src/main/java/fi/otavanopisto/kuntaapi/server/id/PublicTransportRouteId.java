package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing public transport route id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportRouteId extends OrganizationBaseId {

  private static final long serialVersionUID = 590429357437798415L;

  /**
   * Zero-argument constructor for public transport route id
   */
  public PublicTransportRouteId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param organizationId Organization id
   * @param source source
   * @param id id
   */
  public PublicTransportRouteId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_ROUTE;
  }
  
  @Override
  public int getHashInitial() {
    return 341;
  }
  
  @Override
  public int getHashMultiplier() {
    return 353;
  }
  
}
