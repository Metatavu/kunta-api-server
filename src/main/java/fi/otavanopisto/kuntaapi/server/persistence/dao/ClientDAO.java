package fi.otavanopisto.kuntaapi.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.Client;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.Client_;

/**
 * DAO class for Client entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ClientDAO extends AbstractDAO<Client> {
  
  /**
   * Creates new Client entity
   * 
   * @param name client's name
   * @param accessType client's access type
   * @param clientId client's id
   * @param clientSecret client's secret
   * 
   * @return created entity
   */
  public Client create(String name, AccessType accessType, String clientId, String clientSecret) {
    Client client = new Client();
    
    client.setAccessType(accessType);
    client.setClientId(clientId);
    client.setClientSecret(clientSecret);
    client.setName(name);
     
    return persist(client);
  }
  
  /**
   * Finds client by clientId and clientSecret
   * 
   * @param parent parent identifier
   * @param child child identifier
   * @return found identifier relation or null if not found
   */
  public Client findByClientIdAndClientSecret(String clientId, String clientSecret) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Client> criteria = criteriaBuilder.createQuery(Client.class);
    Root<Client> root = criteria.from(Client.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Client_.clientId), clientId),
        criteriaBuilder.equal(root.get(Client_.clientSecret), clientSecret)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
}
