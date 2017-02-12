package fi.otavanopisto.kuntaapi.server.persistence.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.IdentifierRelation;
import fi.otavanopisto.kuntaapi.server.persistence.model.IdentifierRelation_;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier_;

/**
 * DAO class for IdentififerRelation entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class IdentifierRelationDAO extends AbstractDAO<IdentifierRelation> {
  
  /**
   * Creates new Identifier relation entity
   * 
   * @param parent parent identifier
   * @param child child identifier
   * @return created entity
   */
  public IdentifierRelation create(Identifier parent, Identifier child) {
    IdentifierRelation identifierRelation = new IdentifierRelation();
    
    identifierRelation.setParent(parent);
    identifierRelation.setChild(child);
     
    return persist(identifierRelation);
  }

  /**
   * Finds identifier relation by parent and child
   * 
   * @param type identifier type
   * @param source source
   * @param sourceId id in source system
   * @return found identifier or null if non found
   */
  public IdentifierRelation findByParentAndChild(Identifier parent, Identifier child) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<IdentifierRelation> criteria = criteriaBuilder.createQuery(IdentifierRelation.class);
    Root<IdentifierRelation> root = criteria.from(IdentifierRelation.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(IdentifierRelation_.parent), parent),
        criteriaBuilder.equal(root.get(IdentifierRelation_.child), child)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Lists identifier relations where identifier is either parent or child
   * 
   * @param identifier identifier
   * @return identifier relations where identifier is either parent or child
   */
  public List<IdentifierRelation> listByParentOrChild(Identifier identifier) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<IdentifierRelation> criteria = criteriaBuilder.createQuery(IdentifierRelation.class);
    Root<IdentifierRelation> root = criteria.from(IdentifierRelation.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.or(
        criteriaBuilder.equal(root.get(IdentifierRelation_.parent), identifier), 
        criteriaBuilder.equal(root.get(IdentifierRelation_.child), identifier)
      )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  /**
   * Lists identifier relations by child identifier
   * 
   * @param childIdentifier child identifier
   * @return identifier relations by child identifier
   */
  public List<IdentifierRelation> listByChild(Identifier childIdentifier) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<IdentifierRelation> criteria = criteriaBuilder.createQuery(IdentifierRelation.class);
    Root<IdentifierRelation> root = criteria.from(IdentifierRelation.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(IdentifierRelation_.child), childIdentifier)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  /**
   * Lists child identifiers by parent
   * 
   * @param parent parent
   * @return child identifiers by parent
   */
  public List<Identifier> listChildIdentifiersByParentAndType(Identifier parent, String type) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Identifier> criteria = criteriaBuilder.createQuery(Identifier.class);
    Root<IdentifierRelation> root = criteria.from(IdentifierRelation.class);
    Join<IdentifierRelation, Identifier> identifierJoin = root.join(IdentifierRelation_.child);
    
    criteria.select(root.get(IdentifierRelation_.child));
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(IdentifierRelation_.parent), parent),
        criteriaBuilder.equal(identifierJoin.get(Identifier_.type), type)
      )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
}
