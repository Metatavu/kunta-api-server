package fi.metatavu.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.kuntaapi.server.persistence.model.OrganizationExternalAccessToken;
import fi.metatavu.kuntaapi.server.persistence.model.OrganizationExternalAccessToken_;

/**
 * DAO class for OrganizationExternalAccessToken entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class OrganizationExternalAccessTokenDAO extends AbstractDAO<OrganizationExternalAccessToken> {

  /**
   * Creates new OrganizationExternalAccessToken entity
   * 
   * @param organizationKuntaApiId Kunta API id of organization
   * @param accessToken 
   * @param expires 
   * @param tokenType 
   * 
   * @return created OrganizationExternalAccessToken entity
   */
  public OrganizationExternalAccessToken create(String organizationKuntaApiId, String accessToken, OffsetDateTime expires, String tokenType) {
    OrganizationExternalAccessToken organizationSetting = new OrganizationExternalAccessToken();
    
    organizationSetting.setOrganizationKuntaApiId(organizationKuntaApiId);
    organizationSetting.setAccessToken(accessToken);
    organizationSetting.setExpires(expires);
    organizationSetting.setTokenType(tokenType);
    
    return persist(organizationSetting);
  }

  /**
   * Finds organization external access token by tokenType and organizationIdentifier
   * 
   * @param tokenType token type
   * @param organizationKuntaApiId Kunta API id of organization
   * @return found organization external access token or null if not found
   */
  public OrganizationExternalAccessToken findByTokenTypeAndOrganizationKuntaApiId(String tokenType, String organizationKuntaApiId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<OrganizationExternalAccessToken> criteria = criteriaBuilder.createQuery(OrganizationExternalAccessToken.class);
    Root<OrganizationExternalAccessToken> root = criteria.from(OrganizationExternalAccessToken.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(OrganizationExternalAccessToken_.tokenType), tokenType),
        criteriaBuilder.equal(root.get(OrganizationExternalAccessToken_.organizationKuntaApiId), organizationKuntaApiId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Updates organization external access token
   * 
   * @param setting organization setting
   * @param accessToken new token
   * @return updated organization external access token
   */
  public OrganizationExternalAccessToken updateAccessToken(OrganizationExternalAccessToken organizationExternalAccessToken, String accessToken) {
    organizationExternalAccessToken.setAccessToken(accessToken);
    return persist(organizationExternalAccessToken);
  }
  
  /**
   * Updates organization external access token
   * 
   * @param setting organization setting
   * @param expires expires
   * @return updated organization external access token
   */
  public OrganizationExternalAccessToken updateExpires(OrganizationExternalAccessToken organizationExternalAccessToken, OffsetDateTime expires) {
    organizationExternalAccessToken.setExpires(expires);
    return persist(organizationExternalAccessToken);
  }
  
}
