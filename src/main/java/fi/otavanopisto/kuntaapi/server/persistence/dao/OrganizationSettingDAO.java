package fi.otavanopisto.kuntaapi.server.persistence.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting_;

/**
 * DAO class for OrganizationSetting entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class OrganizationSettingDAO extends AbstractDAO<OrganizationSetting> {

  /**
   * Creates new OrganizationSetting entity
   * 
   * @param key key of setting. Must by unique among the organization
   * @param value setting value. Not nullable.
   * @param organizationKuntaApiId Kunta API id of organization
   * 
   * @return created OrganizationSetting entity
   */
  public OrganizationSetting create(String key, String value, String organizationKuntaApiId) {
    OrganizationSetting organizationSetting = new OrganizationSetting();
    
    organizationSetting.setKey(key);
    organizationSetting.setValue(value);
    organizationSetting.setOrganizationKuntaApiId(organizationKuntaApiId);
    
    return persist(organizationSetting);
  }

  /**
   * Finds organization setting by key and organizationIdentifier
   * 
   * @param key setting key
   * @param organizationKuntaApiId Kunta API id of organization
   * @return found setting or null if non found
   */
  public OrganizationSetting findByKeyAndOrganizationKuntaApiId(String key, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<OrganizationSetting> criteria = criteriaBuilder.createQuery(OrganizationSetting.class);
    Root<OrganizationSetting> root = criteria.from(OrganizationSetting.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(OrganizationSetting_.key), key),
        criteriaBuilder.equal(root.get(OrganizationSetting_.organizationKuntaApiId), organizationKuntaApiId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Lists organization settings
   * 
   * @param organizationKuntaApiId organization's Kunta API id
   * @return list of organization settings
   */
  public List<OrganizationSetting> listByOrganizationKuntaApiId(String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<OrganizationSetting> criteria = criteriaBuilder.createQuery(OrganizationSetting.class);
    Root<OrganizationSetting> root = criteria.from(OrganizationSetting.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(OrganizationSetting_.organizationKuntaApiId), organizationKuntaApiId)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Lists organization ids by key
   * 
   * @param key key
   * @return list of organization ids with key
   */
  public List<String> listOrganizationKuntaApiIdsByKey(String key) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> criteria = criteriaBuilder.createQuery(String.class);
    Root<OrganizationSetting> root = criteria.from(OrganizationSetting.class);
    criteria.select(root.get(OrganizationSetting_.organizationKuntaApiId));
    criteria.where(
      criteriaBuilder.equal(root.get(OrganizationSetting_.key), key)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  /**
   * Updates organization setting value
   * 
   * @param setting organization setting
   * @param value new value
   * @return updated organization setting
   */
  public OrganizationSetting updateValue(OrganizationSetting setting, String value) {
    setting.setValue(value);
    return persist(setting);
  }
  
}
