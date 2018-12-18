package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing file id
 * 
 * @author Antti Leppä
 */
public class FileId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 9045191573692430082L;

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
  public int getHashInitial() {
    return 141;
  }
  
  @Override
  public int getHashMultiplier() {
    return 153;
  }
  
}
