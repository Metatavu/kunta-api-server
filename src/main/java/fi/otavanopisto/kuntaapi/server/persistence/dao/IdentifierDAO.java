package fi.otavanopisto.kuntaapi.server.persistence.dao;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier_;

/**
 * DAO class for Identififer entity
 * 
 * @author Otavan Opisto
 */
@Dependent
public class IdentifierDAO extends AbstractDAO<Identifier> {
  
  /**
   * Creates new Identifier entity
   * 
   * @param type identifier type
   * @param kuntaApiId Kunta API id 
   * @param source source
   * @param sourceId id in source system
   * @param organizationKuntaApiId 
   * @return created identifier
   */
  public Identifier create(String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    Identifier identifier = new Identifier();
    
    identifier.setType(type);
    identifier.setKuntaApiId(kuntaApiId);
    identifier.setSource(source);
    identifier.setSourceId(sourceId);
    identifier.setOrganizationKuntaApiId(organizationKuntaApiId);
    
    return persist(identifier);
  }

  /**
   * Finds identifier by source, type, source id and organizationKuntaApiId
   * 
   * @param type identifier type
   * @param source source
   * @param sourceId id in source system
   * @return found identifier or null if non found
   */
  public Identifier findByTypeSourceSourceIdAndOrganizationKuntaApiId(String type, String source, String sourceId, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
          criteriaBuilder.equal(root.get(Identifier_.type), type),
          criteriaBuilder.equal(root.get(Identifier_.source), source),
          criteriaBuilder.equal(root.get(Identifier_.sourceId), sourceId),
          organizationKuntaApiId == null 
            ? criteriaBuilder.isNull(root.get(Identifier_.organizationKuntaApiId)) 
            : criteriaBuilder.equal(root.get(Identifier_.organizationKuntaApiId), organizationKuntaApiId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Finds identifier by type, source and Kunta API id
   * 
   * @param type identifier type
   * @param source source
   * @param kuntaApiId Kunta API id 
   * @return found identifier or null if non found
   */
  public Identifier findByTypeSourceAndKuntaApiId(String type, String source, String kuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
          criteriaBuilder.equal(root.get(Identifier_.type), type),
          criteriaBuilder.equal(root.get(Identifier_.source), source),
          criteriaBuilder.equal(root.get(Identifier_.kuntaApiId), kuntaApiId)          
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

}
