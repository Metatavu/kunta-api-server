package fi.metatavu.kuntaapi.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.persistence.model.StoredResource;
import fi.metatavu.kuntaapi.server.persistence.model.StoredResource_;

/**
 * DAO class for StoredResource entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class StoredResourceDAO extends AbstractDAO<StoredResource> {
  
  public StoredResource create(Identifier identifier, String type, String data) {
    StoredResource storedResource = new StoredResource();
    storedResource.setData(data);
    storedResource.setType(type);
    storedResource.setIdentifier(identifier);
    return persist(storedResource);
  }
  
  public StoredResource findByIdentifier(String type, Identifier identifier) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<StoredResource> criteria = criteriaBuilder.createQuery(StoredResource.class);
    Root<StoredResource> root = criteria.from(StoredResource.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(StoredResource_.identifier), identifier),
        criteriaBuilder.equal(root.get(StoredResource_.type), type)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria)); 
  }

  public StoredResource updateData(StoredResource storedResource, String data) {
    storedResource.setData(data);
    return persist(storedResource);
  }
  
}
