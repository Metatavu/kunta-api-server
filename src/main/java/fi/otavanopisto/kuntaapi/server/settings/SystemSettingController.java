package fi.otavanopisto.kuntaapi.server.settings;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.SystemSettingDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.SystemSetting;

/**
 * Controller for system settings. Class does not handle concurrency so caller must take care of that
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class SystemSettingController {
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingDAO systemSettingDAO;
  
  /**
   * Returns system setting by key or defaultValue if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(String key, String defaultValue) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      return systemSetting.getValue();
    }
    
    return defaultValue;
  }
  
  /**
   * Returns system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(String key) {
    return getSettingValue(key, null);
  }
  
  /**
   * Returns comma delimitered system setting by key or defaultValue if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String[] getSettingValues(String key, String[] defaultValue) {
    String value = getSettingValue(key);
    String[] result = StringUtils.split(value, ',');
    if (result == null) {
      return defaultValue;
    }
    
    return result;
  }
  
  /**
   * Returns comma delimitered system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String[] getSettingValues(String key) {
    return getSettingValues(key, null);
  }
  
  /**
   * Updates system setting value
   * 
   * @param key system setting key
   * @param value new setting value
   */
  public void setSettingValue(String key, String value) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      systemSettingDAO.updateValue(systemSetting, value);
    } else {
      createSystemSetting(key, value);
    }
  }
  
  /**
   * Creates a new system setting
   * 
   * @param key key
   * @param value value 
   * @return SystemSetting
   */
  public SystemSetting createSystemSetting(String key, String value) {
    return systemSettingDAO.create(key, value);
  }
  
  /**
   * Updates an system setting
   * 
   * @param systemSetting setting
   * @param value value 
   * @return SystemSetting
   */
  public SystemSetting updateSystemSetting(SystemSetting systemSetting, String value) {
    if (systemSetting == null) {
      logger.severe("Unable to update null setting");
      return null;
    }
    
    return systemSettingDAO.updateValue(systemSetting, value);
  }
  
  /**
   * Finds system setting by id
   * 
   * @param id system setting id
   * @return system setting or null if not found
   */
  public SystemSetting findSystemSetting(String id) {
    return systemSettingDAO.findById(id);
  }

  /**
   * Finds system setting by key
   * 
   * @param key key
   * @return system setting or null if not found
   */
  public SystemSetting findSystemSettingByKey(String key) {
    return systemSettingDAO.findByKey(key);
  }

  /**
   * Deletes an system setting
   * 
   * @param systemSetting setting
   */
  public void deleteSystemSetting(SystemSetting systemSetting) {
    systemSettingDAO.delete(systemSetting);
  }
  
  /**
   * Checks if application is running in test-mode
   * 
   * @return true if application is running in test-mode. Otherwise false
   */
  public boolean inTestMode(){
    return "true".equals(System.getProperty("it-test"));
  }

  /**
   * Returns true when test is running
   * 
   * @return true
   */
  public boolean isTestRunning() {
    return "true".equalsIgnoreCase(getSettingValue(KuntaApiConsts.SYSTEM_SETTING_TESTS_RUNNING));
  }
  
  /**
   * Returns true if application is not in test mode or test is currently running
   * 
   * @return true if application is not in test mode or test is currently running
   */
  public boolean isNotTestingOrTestRunning() {
    return !inTestMode() || isTestRunning();
  }

  /**
   * Returns true if system is running in fail-safe mode
   * 
   * @return whether system is running in fail-safe mode or nor
   */
  public boolean inFailsafeMode() {
    return "true".equalsIgnoreCase(System.getProperty("kuntaApi.failsafeMode"));
  }

  /**
   * Returns whether system setting has a value set or not
   * 
   * @param key key
   * @return whether system setting has a value set or not
   **/
  public boolean hasSettingValue(String key) {
    return StringUtils.isNotBlank(getSettingValue(key));
  }

}
