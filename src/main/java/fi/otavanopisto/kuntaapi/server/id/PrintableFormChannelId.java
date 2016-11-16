package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing printable form service channel id
 * 
 * @author Antti Leppä
 */
public class PrintableFormChannelId extends BaseId {

  /**
   * Zero-argument constructor for printable form service channel id
   */
  public PrintableFormChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PrintableFormChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PRINTABLE_FORM_CHANNEL;
  }
  
  @Override
  protected int getHashInitial() {
    return 191;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 203;
  }
  
}
