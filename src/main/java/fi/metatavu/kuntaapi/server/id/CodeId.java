package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing webpage service channel id
 * 
 * @author Antti Leppä
 */
public class CodeId extends BaseId {

  private static final long serialVersionUID = 6862158839726959904L;

  /**
   * Zero-argument constructor for webpage service channel id
   */
  public CodeId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public CodeId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.CODE;
  }
  
  @Override
  public int getHashInitial() {
    return 349;
  }
  
  @Override
  public int getHashMultiplier() {
    return 361;
  }
  
}
