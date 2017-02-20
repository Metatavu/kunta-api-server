package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing public transport stop id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportStopId extends OrganizationBaseId {

  private static final long serialVersionUID = -3906214604876703838L;
  
  /**
   * Zero-argument constructor for public transport route id
   */
  public PublicTransportStopId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param organizationId Organization id
   * @param source source
   * @param id id
   */
  public PublicTransportStopId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_STOP;
  }
  
  @Override
  protected int getHashInitial() {
    return 343;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 355;
  }
  
}
