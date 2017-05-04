package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for palvelutietoväylä
 * 
 * @author Antti Leppä
 */
public class PtvConsts {
  
  public static final String IDENTIFIER_NAME = "PTV";
  public static final boolean SYNCHRONIZE = true;
  public static final String CACHE_NAME = "ptv";
  public static final String SYSTEM_SETTING_BASEURL = "ptv.baseUrl";
  public static final String DEFAULT_LANGUAGE = "fi";
  public static final String TIMEZONE = "Europe/Helsinki";
  public static final List<String> PTV_SUPPORTED_LANGUAGES = Collections.unmodifiableList(Arrays.asList("fi", "en", "sv"));
  
  private PtvConsts() {
  }

}
