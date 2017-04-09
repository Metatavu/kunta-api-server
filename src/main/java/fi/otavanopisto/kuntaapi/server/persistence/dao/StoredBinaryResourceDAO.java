package fi.otavanopisto.kuntaapi.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.StoredBinaryResource;
import fi.otavanopisto.kuntaapi.server.persistence.model.StoredBinaryResource_;

/**
 * DAO class for StoredBinaryResource entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class StoredBinaryResourceDAO extends AbstractDAO<StoredBinaryResource> {
  
  public StoredBinaryResource create(Identifier identifier, String type, String contentType, byte[] data) {
    StoredBinaryResource storedBinaryResource = new StoredBinaryResource();
    storedBinaryResource.setContentType(contentType);
    storedBinaryResource.setData(data);
    storedBinaryResource.setType(type);
    storedBinaryResource.setIdentifier(identifier);
    return persist(storedBinaryResource);
  }
  
  public StoredBinaryResource findByIdentifier(String type, Identifier identifier) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<StoredBinaryResource> criteria = criteriaBuilder.createQuery(StoredBinaryResource.class);
    Root<StoredBinaryResource> root = criteria.from(StoredBinaryResource.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(StoredBinaryResource_.identifier), identifier),
        criteriaBuilder.equal(root.get(StoredBinaryResource_.type), type)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria)); 
  }

  public StoredBinaryResource updateData(StoredBinaryResource storedBinaryResource, byte[] data) {
    storedBinaryResource.setData(data);
    return persist(storedBinaryResource);
  }

  public StoredBinaryResource updateContentType(StoredBinaryResource storedBinaryResource, String contentType) {
    storedBinaryResource.setContentType(contentType);
    return persist(storedBinaryResource);
  }
  
}
