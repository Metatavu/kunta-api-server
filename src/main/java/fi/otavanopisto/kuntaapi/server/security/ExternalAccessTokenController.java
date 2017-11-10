package fi.otavanopisto.kuntaapi.server.security;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.OrganizationExternalAccessTokenDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationExternalAccessToken;

/**
 * Controller for external access tokens.
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExternalAccessTokenController {

  private static final String FAILED_TO_TRANSLATE = "Failed to translate %s into KuntaApiId id";

  @Inject
  private Logger logger;

  @Inject
  private OrganizationExternalAccessTokenDAO organizationExternalAccessTokenDAO;
  
  @Inject
  private IdController idController;
  
  /**
   * Creates new organization external access token 
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @param expires the time the token expires
   * @return Updated organization external access token 
   */
  public OrganizationExternalAccessToken createOrganizationExternalAccessToken(OrganizationId organizationId, String tokenType, String accessToken, OffsetDateTime expires) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId != null) {
      return organizationExternalAccessTokenDAO.create(organizationId.getId(), accessToken, expires, tokenType);
    } else {
      logger.severe(() -> String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return null;
    }
  }
  
  /**
   * Returns organization external access token by token type
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @return setting value
   */
  public OrganizationExternalAccessToken getOrganizationExternalAccessToken(OrganizationId organizationId, String tokenType) {
    OrganizationId kuntaApiId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId != null) {
      return organizationExternalAccessTokenDAO.findByTokenTypeAndOrganizationKuntaApiId(tokenType, kuntaApiId.getId());
    } else {
      logger.severe(() -> String.format(FAILED_TO_TRANSLATE, organizationId.toString()));
      return null;
    }
  }
  
  /**
   * Updates organization external access token 
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @param accessToken access token
   * @param expires the time the token expires
   * @return Updated organization external access token 
   */
  public OrganizationExternalAccessToken updateOrganizationExternalAccessToken(OrganizationExternalAccessToken organizationExternalAccessToken, String accessToken, OffsetDateTime expires) {
    organizationExternalAccessTokenDAO.updateAccessToken(organizationExternalAccessToken, accessToken);
    return organizationExternalAccessTokenDAO.updateExpires(organizationExternalAccessToken, expires);
  }

  /**
   * Creates or updates organization external access token
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @param accessToken access token
   * @param expires the time the token expires
   */
  public void setOrganizationExternalAccessToken(OrganizationId organizationId, String tokenType, String accessToken, OffsetDateTime expires) {
    OrganizationExternalAccessToken externalAccessToken = getOrganizationExternalAccessToken(organizationId, tokenType);
    if (externalAccessToken == null) {
      createOrganizationExternalAccessToken(organizationId, tokenType, accessToken, expires);
    } else {
      updateOrganizationExternalAccessToken(externalAccessToken, accessToken, expires);
    }
  }

  /**
   * Returns organization external access token value
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @return access token as string or null if not found
   */
  public String getOrganizationExternalAccessTokenValue(OrganizationId organizationId, String tokenType) {
    OrganizationExternalAccessToken externalAccessToken = getOrganizationExternalAccessToken(organizationId, tokenType);
    if (externalAccessToken != null) {
      return externalAccessToken.getAccessToken();
    }
    
    return null;
  }

  /**
   * Returns organization external access token expire time
   * 
   * @param organizationId organization id
   * @param tokenType token type
   * @return access token expire time or null if token is not defined
   */
  public OffsetDateTime getOrganizationExternalAccessTokenExpires(OrganizationId organizationId, String tokenType) {
    OrganizationExternalAccessToken externalAccessToken = getOrganizationExternalAccessToken(organizationId, tokenType);
    if (externalAccessToken != null) {
      return externalAccessToken.getExpires();
    }
    
    return null;
  }
  
}
