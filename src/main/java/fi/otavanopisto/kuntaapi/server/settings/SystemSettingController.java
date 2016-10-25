package fi.otavanopisto.kuntaapi.server.settings;

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.persistence.dao.SystemSettingDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.SystemSetting;

/**
 * Controller for system settings. Class does not handle concurrency so caller must take care of that
 * 
 * @author Antti Leppä
 */
@Dependent
public class SystemSettingController {
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingDAO systemSettingDAO;
  
  /**
   * Returns system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(String key) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      return systemSetting.getValue();
    }
    
    return null;
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
  
}
