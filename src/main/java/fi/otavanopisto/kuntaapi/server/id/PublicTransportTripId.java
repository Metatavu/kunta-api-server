package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing public transport trip id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportTripId extends OrganizationBaseId {

  private static final long serialVersionUID = 790750261270735849L;
  
  /**
   * Zero-argument constructor for public transport route id
   */
  public PublicTransportTripId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param organizationId Organization id
   * @param source source
   * @param id id
   */
  public PublicTransportTripId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_TRIP;
  }
  
  @Override
  public int getHashInitial() {
    return 347;
  }
  
  @Override
  public int getHashMultiplier() {
    return 359;
  }
  
}
