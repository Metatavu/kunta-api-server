package fi.metatavu.kuntaapi.server.integrations.ptv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for palvelutietoväylä
 * 
 * @author Antti Leppä
 */
public class PtvConsts {
  
  public static final String VERSION = "v9";
  public static final String IDENTIFIER_NAME = "PTV";
  public static final boolean SYNCHRONIZE = true;
  public static final String CACHE_NAME = "ptv";
  public static final String SYSTEM_SETTING_BASEURL = "ptv.baseUrl";
  public static final String SYSTEM_SETTING_STS_BASEURL = "ptv.stsBaseUrl";
  public static final String SYSTEM_SETTING_AUTH_STRATEGY = "ptv.authStrategy";
  public static final String SYSTEM_SETTING_FAILSAFE_ORGANIZATION_UPDATER_ENABLED = "ptv.failsafeOrganizationUpdaterEnabled";
  public static final String SYSTEM_SETTING_PRIORITY_ORGANIZATION_UPDATER_ENABLED = "ptv.priorityOrganizationUpdaterEnabled";
  public static final String SYSTEM_SETTING_FAILSAFE_SERVICE_CHANNEL_UPDATER_ENABLED = "ptv.failsafeServiceChannelUpdaterEnabled";
  public static final String SYSTEM_SETTING_PRIORITY_SERVICE_CHANNEL_UPDATER_ENABLED = "ptv.priorityServiceChannelUpdaterEnabled";
  public static final String SYSTEM_SETTING_FAILSAFE_SERVICE_UPDATER_ENABLED = "ptv.failsafeServiceUpdaterEnabled";
  public static final String SYSTEM_SETTING_PRIORITY_SERVICE_UPDATER_ENABLED = "ptv.priorityServiceUpdaterEnabled";
  public static final String DEFAULT_LANGUAGE = "fi";
  public static final String TIMEZONE = "Europe/Helsinki";
  public static final List<String> PTV_SUPPORTED_LANGUAGES = Collections.unmodifiableList(Arrays.asList("fi", "en", "sv"));
  public static final String ORGANIZATION_SETTING_API_USER = "ptv.apiuser";
  public static final String ORGANIZATION_SETTING_API_PASS = "ptv.apipass";
  public static final String PTV_ACCESS_TOKEN_TYPE = "ptv";
  public static final String PUBLISHED_STATUS = "Published";
  
  private PtvConsts() {
  }
  
}
