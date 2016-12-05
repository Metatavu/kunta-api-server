package fi.otavanopisto.kuntaapi.server.integrations.management;

/**
 * Constants for management Wordpress integration
 * 
 * @author Antti Leppä
 */
public class ManagementConsts {
  
  public static final String IDENTIFIER_NAME = "MWP";
  public static final boolean SYNCHRONIZE = false;
  public static final String CACHE_NAME = "mwp";
  public static final boolean CACHE_RESPONSES = false;
  public static final String ORGANIZATION_SETTING_BASEURL = "managementservice.baseUrl";
  public static final String DEFAULT_LOCALE = "fi";

  private ManagementConsts() {
  }
  
}
