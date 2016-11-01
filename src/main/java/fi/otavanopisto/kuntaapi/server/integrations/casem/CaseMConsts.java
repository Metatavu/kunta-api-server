package fi.otavanopisto.kuntaapi.server.integrations.casem;

/**
 * Constants for CaseM
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public class CaseMConsts {
  
  public static final String IDENTIFIER_NAME = "CASEM";
  public static final String ORGANIZATION_SETTING_BASEURL = "casem.baseUrl";
  public static final String ORGANIZATION_SETTING_DOWNLOAD_PATH = "casem.downloadPath";
  public static final String ORGANIZATION_SETTING_ROOT_NODE = "casem.rootNode";
  public static final String ORGANIZATION_SETTING_LOCALE_ID = "casem.locale_%d";
  public static final String CACHE_NAME = "casem";
  public static final boolean CACHE_RESPONSES = false;
  public static final String DEFAULT_LANGUAGE = "fi";
  public static final String SERVER_TIMEZONE_ID = "Europe/Helsinki";
  
  private CaseMConsts() {
  }
  
}
