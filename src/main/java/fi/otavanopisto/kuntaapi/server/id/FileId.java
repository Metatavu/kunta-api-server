package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing file id
 * 
 * @author Antti Leppä
 */
public class FileId extends OrganizationBaseId {
  
  /**
   * Zero-argument constructor for file id
   */
  public FileId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public FileId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.FILE;
  }
  
  @Override
  protected int getHashInitial() {
    return 141;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 153;
  }
  
}
