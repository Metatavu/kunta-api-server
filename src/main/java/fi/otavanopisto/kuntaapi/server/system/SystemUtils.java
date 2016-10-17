package fi.otavanopisto.kuntaapi.server.system;

/**
 * 
 * System utilities
 * 
 * @author Heikki Kurhinen
 */
public class SystemUtils {
  
  private SystemUtils(){};
  
  /**
   * Checks if application is running in test-mode
   * 
   * @return true if application is running in test-mode. Otherwise false
   */
  public static boolean inTestMode(){
    return "true".equals(System.getProperty("it-test"));
  }
}
