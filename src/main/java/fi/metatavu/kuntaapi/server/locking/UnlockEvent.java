package fi.metatavu.kuntaapi.server.locking;

/**
 * Event for releasing cluster-wide locks
 * 
 * @author Antti Leppä
 */
public class UnlockEvent {

  private final String key;

  /**
   * Constructor key
   * 
   * @param key lock key
   */
  public UnlockEvent(String key) {
    super();
    this.key = key;
  }
  
  /**
   * Returns lock key
   * 
   * @return lock key
   */
  public String getKey() {
    return key;
  }
  
}
