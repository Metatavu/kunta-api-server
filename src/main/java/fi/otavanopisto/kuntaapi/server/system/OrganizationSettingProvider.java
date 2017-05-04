package fi.otavanopisto.kuntaapi.server.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import javax.enterprise.context.ApplicationScoped;

/**
 * Provides organization settings 
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings("squid:S3306")
public class OrganizationSettingProvider {

  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private Logger logger;
  
  /**
   * Creates organization setting
   * 
   * @param organizationId Id of organization
   * @param key Key for setting
   * @param value Value for setting
   * @return organizationSetting
   */
  public OrganizationSetting createOrganizationSetting(OrganizationId organizationId, String key, String value){
    return createOrganizationEntity(organizationSettingController.createOrganizationSetting(key, value, organizationId));
  }
  
  /**
   * List organization settings
   * 
   * @param organizationId Id of organization
   * @param key key of setting
   * @return List of organization settings
   */
  public List<OrganizationSetting> listOrganizationSettings(OrganizationId organizationId, String key) {
    List<fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting> settings;

    if (StringUtils.isNotBlank(key)) {
      fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting setting = organizationSettingController.findOrganizationSettingByKey(organizationId, key);
      if (setting != null) {
        settings = Collections.singletonList(setting);
      } else {
        settings = Collections.emptyList();
      }
    } else {
      settings = organizationSettingController.listOrganizationSettings(organizationId);
    }

    List<OrganizationSetting> result = new ArrayList<>(settings.size());
    for (fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting setting : settings) {
      result.add(createOrganizationEntity(setting));
    }

    return result;
  }

  /**
   * Finds organization setting with organizationId and SettingId
   * 
   * @param organizationId id of organization
   * @param settingId id of organizationSetting
   * @return OrganizationSetting
   */
  public OrganizationSetting findOrganizationSetting(OrganizationId organizationId, Long settingId) {
    fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting organizationSetting = organizationSettingController.findOrganizationSetting(settingId);
    if (organizationSetting == null) {
      return null;
    }

    if (!StringUtils.equals(organizationSetting.getOrganizationKuntaApiId(), organizationId.getId())) {
      logger.severe(String.format("Tried to access organization setting %s with organization %s", settingId, organizationId.toString()));
      return null;
    }

    return createOrganizationEntity(organizationSetting);
  }

  /**
   * Updated organizationSetting
   * 
   * @param organizationSettingId id of organization setting
   * @param value new value for organization setting
   * @return updated organization setting
   */
  public OrganizationSetting updateOrganizationSetting(Long organizationSettingId, String value) {
    fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting organizationSetting = organizationSettingController.findOrganizationSetting(organizationSettingId);
    if (organizationSetting == null) {
      logger.log(Level.SEVERE, String.format("Could not find organizationSetting by id %s", organizationSettingId));
      return null;
    }
    
    return createOrganizationEntity(organizationSettingController.updateOrganizationSetting(organizationSetting, value));
  }
  
  /**
   * Deletes organization setting
   * 
   * @param organizationSettingId of organization setting
   */
  public void deleteOrganizationSetting(Long organizationSettingId) {
    fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting organizationSetting = organizationSettingController.findOrganizationSetting(organizationSettingId);
    if (organizationSetting != null) {
      organizationSettingController.deleteOrganizationSetting(organizationSetting);
    }
  }

  private OrganizationSetting createOrganizationEntity(fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting organizationSetting) {
    if (organizationSetting == null) {
      return null;
    }
    OrganizationSetting result = new OrganizationSetting();
    result.setId(String.valueOf(organizationSetting.getId()));
    result.setKey(organizationSetting.getKey());
    result.setValue(organizationSetting.getValue());
    return result;
  }

}
