package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing attachment id
 * 
 * @author Antti Leppä
 */
public class AttachmentId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -4920851211464070298L;

  /**
   * Zero-argument constructor for attachment id
   */
  public AttachmentId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public AttachmentId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ATTACHMENT;
  }
  
  @Override
  protected int getHashInitial() {
    return 125;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 137;
  }

}
