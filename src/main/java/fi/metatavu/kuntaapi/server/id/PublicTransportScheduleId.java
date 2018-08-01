package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing fragment id
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class PublicTransportScheduleId extends OrganizationBaseId {

  private static final long serialVersionUID = 590429357437798415L;

  /**
   * Zero-argument constructor for public transport schedule id
   */
  public PublicTransportScheduleId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PublicTransportScheduleId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PUBLIC_TRANSPORT_SCHEDULE;
  }
  
  @Override
  public int getHashInitial() {
    return 339;
  }
  
  @Override
  public int getHashMultiplier() {
    return 351;
  }
  
}
