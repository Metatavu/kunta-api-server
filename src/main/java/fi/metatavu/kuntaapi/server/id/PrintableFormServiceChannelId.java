package fi.metatavu.kuntaapi.server.id;

/**
 * Class representing printable form service channel id
 * 
 * @author Antti Leppä
 */
public class PrintableFormServiceChannelId extends BaseId {

  private static final long serialVersionUID = 3556104745639774281L;

  /**
   * Zero-argument constructor for printable form service channel id
   */
  public PrintableFormServiceChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PrintableFormServiceChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PRINTABLE_FORM_SERVICE_CHANNEL;
  }
  
  @Override
  public int getHashInitial() {
    return 191;
  }
  
  @Override
  public int getHashMultiplier() {
    return 203;
  }
  
}
