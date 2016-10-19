package fi.otavanopisto.kuntaapi.server.settings;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.OrganizationSettingDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;

/**
 * Controller for organization settings. Class does not handle concurrency so caller must take care of that
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class OrganizationSettingController implements Serializable {
  
  private static final long serialVersionUID = 2199544783912659348L;

  private static final String FAILED_TO_TRANSLATE = "Failed to translate %s into KuntaApiId id";

  @Inject
  private transient Logger logger;

  @Inject
  private OrganizationSettingDAO organizationSettingDAO;
  
  @Inject
  private IdController idController;
  
  private OrganizationSettingController() {
  }
  
  /**
   * Returns organization setting by key or null if setting is not defined
   * 
   * @param organizationId organization id
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(OrganizationId organizationId, String key) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId != null) {
      OrganizationSetting organizationSetting = organizationSettingDAO.findByKeyAndOrganizationKuntaApiId(key, kuntaApiId.getId());
      if (organizationSetting != null) {
        return organizationSetting.getValue();
      }
      
      return null;
    } else {
      logger.severe(String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return null;
    }
  }
  
  /**
   * Updates system setting value
   * 
   * @param key system setting key
   * @param value new setting value
   * @param organizationId organization's id
   */
  public void setSettingValue(OrganizationId organizationId, String key, String value) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
    } else {
      OrganizationSetting organizationSetting = organizationSettingDAO.findByKeyAndOrganizationKuntaApiId(key, kuntaApiId.getId());
      if (organizationSetting != null) {
        organizationSettingDAO.updateValue(organizationSetting, value);
      } else {
        createOrganizationSetting(key, value, kuntaApiId);
      }
    }
  }
  
  /**
   * Creates a new organization setting
   * 
   * @param key key
   * @param value value 
   * @param organizationKuntaApiId organization's Kunta API Id
   * @return OrganizationSetting
   */
  public OrganizationSetting createOrganizationSetting(String key, String value, OrganizationId organizationId) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return null;
    } else {
      return organizationSettingDAO.create(key, value, organizationId.getId());
    } 
  }
  
  /**
   * Updates an organization setting
   * 
   * @param organizationSetting setting
   * @param value value 
   * @return OrganizationSetting
   */
  public OrganizationSetting updateOrganizationSetting(OrganizationSetting organizationSetting, String value) {
    if (organizationSetting == null) {
      logger.severe("Unable to update null setting");
      return null;
    }
    
    return organizationSettingDAO.updateValue(organizationSetting, value);
  }
  
  /**
   * Lists organization's keys
   * 
   * @param organizationKuntaApiId organization's Kunta API id
   * @return list of organization's keys
   */
  public List<OrganizationSetting> listOrganizationSettings(OrganizationId organizationId) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return Collections.emptyList();
    } else {
      return organizationSettingDAO.listByOrganizationKuntaApiId(kuntaApiId.getId());
    }
  }
  
  /**
   * Finds organization setting by id
   * 
   * @param id organization setting id
   * @return organization setting or null if not found
   */
  public OrganizationSetting findOrganizationSetting(String id) {
    return organizationSettingDAO.findById(id);
  }

  /**
   * Finds organization setting by key
   * 
   * @param organizationId organization id
   * @param key key
   * @return organization setting or null if not found
   */
  public OrganizationSetting findOrganizationSettingByKey(OrganizationId organizationId, String key) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return null;
    } else {
      return organizationSettingDAO.findByKeyAndOrganizationKuntaApiId(key, organizationId.getId());
    }
  }

  /**
   * Deletes an organization setting
   * 
   * @param organizationSetting setting
   */
  public void deleteOrganizationSetting(OrganizationSetting organizationSetting) {
    organizationSettingDAO.delete(organizationSetting);
  }
  
}
