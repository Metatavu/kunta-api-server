package fi.otavanopisto.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
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
  public Identifier create(Identifier parent, Long orderIndex, String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    Identifier identifier = new Identifier();
    
    identifier.setParent(parent);
    identifier.setOrderIndex(orderIndex);
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
   * Finds identifier by type, kuntaApiId and organizationKuntaApiId
   * 
   * @param type identifier type
   * @param source source
   * @param sourceId id in source system
   * @return found identifier or null if non found
   */
  public Identifier findByTypeAndKuntaApiIdAndOrganizationKuntaApiId(String type, String kuntaApiId, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
          criteriaBuilder.equal(root.get(Identifier_.type), type),
          criteriaBuilder.equal(root.get(Identifier_.kuntaApiId), kuntaApiId),
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

  public List<Identifier> listBySourceAndType(String source, String type) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Identifier_.type), type),
        criteriaBuilder.equal(root.get(Identifier_.source), source)
      )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }

  public List<Identifier> listByOrganizationIdAndSourceAndType(String organizationKuntaApiId, String source, String type) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Identifier_.organizationKuntaApiId), organizationKuntaApiId),
        criteriaBuilder.equal(root.get(Identifier_.type), type),
        criteriaBuilder.equal(root.get(Identifier_.source), source)
      )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }

  public Identifier updateOrderIndex(Identifier identifier, Long orderIndex) {
    identifier.setOrderIndex(orderIndex);
    return persist(identifier);
  }
  
  public Identifier updateParent(Identifier identifier, Identifier parent) {
    identifier.setParent(parent);
    return persist(identifier);
  }
  
  public Identifier updateModified(Identifier identifier, OffsetDateTime modified) {
    identifier.setModified(modified);
    return persist(identifier);
  }

  public Long findOrderIndexByKuntaApiIdentifier(String kuntaApiIdentifier) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
    Root<Identifier> root = criteria.from(Identifier.class);
    criteria.select(root.get(Identifier_.orderIndex));
    criteria.where(
      criteriaBuilder.equal(root.get(Identifier_.kuntaApiId), kuntaApiIdentifier)
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  
}
