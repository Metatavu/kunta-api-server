package fi.metatavu.kuntaapi.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.persistence.model.clients.Client;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermissionGrant;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermissionGrant_;

/**
 * DAO class for clientOrganizationPermissionGrant entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ClientOrganizationPermissionGrantDAO extends AbstractDAO<ClientOrganizationPermissionGrant> {

  /**
  * Creates new clientOrganizationPermissionGrant
  *
  * @param client client
  * @param organizationIdentifier organizationIdentifier
  * @param permission permission
  * @return created clientOrganizationPermissionGrant
  */
  public ClientOrganizationPermissionGrant create(Client client, Identifier organizationIdentifier, ClientOrganizationPermission permission) {
    ClientOrganizationPermissionGrant clientOrganizationPermissionGrant = new ClientOrganizationPermissionGrant();
    clientOrganizationPermissionGrant.setClient(client);
    clientOrganizationPermissionGrant.setOrganizationIdentifier(organizationIdentifier);
    clientOrganizationPermissionGrant.setPermission(permission);
    return persist(clientOrganizationPermissionGrant);
  }
  
  /**
   * Returns ClientOrganizationPermissionGrant for client, organizationIdentifier and permission
   * 
   * @param client client
   * @param organizationIdentifier organizationIdentifier
   * @param permission permission
   * @return ClientOrganizationPermissionGrant or null if not found
   */
  public ClientOrganizationPermissionGrant findByClientOrganizationIdentifierAndPermission(Client client, Identifier organizationIdentifier, ClientOrganizationPermission permission) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ClientOrganizationPermissionGrant> criteria = criteriaBuilder.createQuery(ClientOrganizationPermissionGrant.class);
    Root<ClientOrganizationPermissionGrant> root = criteria.from(ClientOrganizationPermissionGrant.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ClientOrganizationPermissionGrant_.client), client),
        criteriaBuilder.equal(root.get(ClientOrganizationPermissionGrant_.organizationIdentifier), organizationIdentifier),
        criteriaBuilder.equal(root.get(ClientOrganizationPermissionGrant_.permission), permission)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
  * Updates client
  *
  * @param client client
  * @return updated clientOrganizationPermissionGrant
  */
  public ClientOrganizationPermissionGrant updateClient(ClientOrganizationPermissionGrant clientOrganizationPermissionGrant, Client client) {
    clientOrganizationPermissionGrant.setClient(client);
    return persist(clientOrganizationPermissionGrant);
  }

  /**
  * Updates organizationIdentifier
  *
  * @param organizationIdentifier organizationIdentifier
  * @return updated clientOrganizationPermissionGrant
  */
  public ClientOrganizationPermissionGrant updateOrganizationIdentifier(ClientOrganizationPermissionGrant clientOrganizationPermissionGrant, Identifier organizationIdentifier) {
    clientOrganizationPermissionGrant.setOrganizationIdentifier(organizationIdentifier);
    return persist(clientOrganizationPermissionGrant);
  }

  /**
  * Updates permission
  *
  * @param permission permission
  * @return updated clientOrganizationPermissionGrant
  */
  public ClientOrganizationPermissionGrant updatePermission(ClientOrganizationPermissionGrant clientOrganizationPermissionGrant, ClientOrganizationPermission permission) {
    clientOrganizationPermissionGrant.setPermission(permission);
    return persist(clientOrganizationPermissionGrant);
  }

}
