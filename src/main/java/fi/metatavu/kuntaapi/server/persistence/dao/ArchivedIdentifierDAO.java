package fi.metatavu.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.kuntaapi.server.persistence.model.ArchivedIdentifier;
import fi.metatavu.kuntaapi.server.persistence.model.ArchivedIdentifier_;

/**
 * DAO class for Identififer entity
 * 
 * @author Otavan Opisto
 */
@ApplicationScoped
public class ArchivedIdentifierDAO extends AbstractDAO<ArchivedIdentifier> {
  
  /**
   * Creates new ArchivedIdentifier entity
   * 
   * @param type identifier type
   * @param kuntaApiId Kunta API id 
   * @param source source
   * @param sourceId id in source system
   * @param organizationKuntaApiId 
   * @param archived time when identifier was archived
   * @return created archived identifier
   */
  public ArchivedIdentifier create(String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId, Long orderIndex, OffsetDateTime archived) {
    ArchivedIdentifier archivedIdentifier = new ArchivedIdentifier();
    
    archivedIdentifier.setType(type);
    archivedIdentifier.setKuntaApiId(kuntaApiId);
    archivedIdentifier.setOrderIndex(orderIndex);
    archivedIdentifier.setSource(source);
    archivedIdentifier.setSourceId(sourceId);
    archivedIdentifier.setOrganizationKuntaApiId(organizationKuntaApiId);
    archivedIdentifier.setArchived(archived);
    
    return persist(archivedIdentifier);
  }

  /**
   * Finds identifier by source, type, source id and organizationKuntaApiId
   * 
   * @param type identifier type
   * @param source source
   * @param sourceId id in source system
   * @return found identifier or null if non found
   */
  public ArchivedIdentifier findByTypeSourceSourceIdAndOrganizationKuntaApiId(String type, String source, String sourceId, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ArchivedIdentifier> criteria = criteriaBuilder.createQuery(ArchivedIdentifier.class);
    Root<ArchivedIdentifier> root = criteria.from(ArchivedIdentifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
          criteriaBuilder.equal(root.get(ArchivedIdentifier_.type), type),
          criteriaBuilder.equal(root.get(ArchivedIdentifier_.source), source),
          criteriaBuilder.equal(root.get(ArchivedIdentifier_.sourceId), sourceId),
          organizationKuntaApiId == null 
            ? criteriaBuilder.isNull(root.get(ArchivedIdentifier_.organizationKuntaApiId)) 
            : criteriaBuilder.equal(root.get(ArchivedIdentifier_.organizationKuntaApiId), organizationKuntaApiId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Finds archived identifier by type, kuntaApiId and organizationKuntaApiId
   * 
   * @param type identifier type
   * @param source source
   * @param sourceId id in source system
   * @return found identifier or null if non found
   */
  public ArchivedIdentifier findByTypeAndKuntaApiIdAndOrganizationKuntaApiId(String type, String kuntaApiId, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ArchivedIdentifier> criteria = criteriaBuilder.createQuery(ArchivedIdentifier.class);
    Root<ArchivedIdentifier> root = criteria.from(ArchivedIdentifier.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
          criteriaBuilder.equal(root.get(ArchivedIdentifier_.type), type),
          criteriaBuilder.equal(root.get(ArchivedIdentifier_.kuntaApiId), kuntaApiId),
          organizationKuntaApiId == null 
            ? criteriaBuilder.isNull(root.get(ArchivedIdentifier_.organizationKuntaApiId)) 
            : criteriaBuilder.equal(root.get(ArchivedIdentifier_.organizationKuntaApiId), organizationKuntaApiId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
}
