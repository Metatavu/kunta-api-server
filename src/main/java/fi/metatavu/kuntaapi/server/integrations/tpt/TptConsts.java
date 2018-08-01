package fi.metatavu.kuntaapi.server.integrations.tpt;

/**
 * Constants for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1075")
public class TptConsts {
  
  public static final String IDENTIFIER_NAME = "TPT";
  public static final String ORGANIZATION_SETTING_AREA = "tpt.area";
  public static final String SYSTEM_SETTING_BASE_URL = "tpt.baseUrl";
  public static final String DEFAULT_BASE_URL = "https://paikat.te-palvelut.fi";
  public static final String AREA_SEARCH_PATH = "/tpt-api/tyopaikat?alueet=%s";
  public static final String JOB_LINK_PATH = "/tpt/%d";
  public static final String TIMEZONE = "Europe/Helsinki";
  
  private TptConsts() {
  }
  
}
