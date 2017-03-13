package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing event id
 * 
 * @author Antti Leppä
 */
public class EventId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -1583566805362578411L;

  /**
   * Zero-argument constructor for event id
   */
  public EventId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public EventId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.EVENT;
  }
  
  @Override
  public int getHashInitial() {
    return 123;
  }
  
  @Override
  public int getHashMultiplier() {
    return 135;
  }
  
}
