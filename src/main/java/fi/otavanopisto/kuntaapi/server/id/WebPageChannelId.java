package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing webpage service channel id
 * 
 * @author Antti Leppä
 */
public class WebPageChannelId extends BaseId {

  private static final long serialVersionUID = 4485290448691923574L;

  /**
   * Zero-argument constructor for webpage service channel id
   */
  public WebPageChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public WebPageChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.WEBPAGE_CHANNEL;
  }
  
  @Override
  protected int getHashInitial() {
    return 231;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 243;
  }

}
