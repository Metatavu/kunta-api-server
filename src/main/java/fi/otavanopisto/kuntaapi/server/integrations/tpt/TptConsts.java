package fi.otavanopisto.kuntaapi.server.integrations.tpt;

/**
 * Constants for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
public class TptConsts {
  
  public static final String IDENTIFIER_NAME = "TPT";
  public static final String ORGANIZATION_AREA = "tpt.area";
  public static final String AREA_SEARCH_URL = "https://paikat.te-palvelut.fi/tpt-api/tyopaikat?alueet=%s";
  public static final String JOB_LINK_URL = "https://paikat.te-palvelut.fi/tpt/%d";
  public static final String TIMEZONE = "Europe/Helsinki";
  
  private TptConsts() {
  }
  
}
