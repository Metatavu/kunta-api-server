package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing public transport stop time id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportStopTimeId extends OrganizationBaseId {

  private static final long serialVersionUID = 5437810682455218230L;

  /**
   * Zero-argument constructor for public transport route id
   */
  public PublicTransportStopTimeId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param organizationId Organization id
   * @param source source
   * @param id id
   */
  public PublicTransportStopTimeId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_STOPTIME;
  }
  
  @Override
  public int getHashInitial() {
    return 345;
  }
  
  @Override
  public int getHashMultiplier() {
    return 357;
  }
  
}
