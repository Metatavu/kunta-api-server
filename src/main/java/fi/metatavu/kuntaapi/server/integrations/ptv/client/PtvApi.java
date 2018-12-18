package fi.metatavu.kuntaapi.server.integrations.ptv.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.CodeListApi;
import fi.metatavu.ptv.client.ConnectionApi;
import fi.metatavu.ptv.client.GeneralDescriptionApi;
import fi.metatavu.ptv.client.OrganizationApi;
import fi.metatavu.ptv.client.ServiceApi;
import fi.metatavu.ptv.client.ServiceChannelApi;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.security.ExternalAccessTokenController;

/**
 * PTV API Class
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PtvApi {
  
  @Inject
  private PtvClient client;

  @Inject
  private ExternalAccessTokenController externalAccessTokenController;
  
  /**
   * Returns OrganizationApi
   * 
   * @return OrganizationApi
   */
  public OrganizationApi getOrganizationApi() {
    return new OrganizationApi(client, null);
  }
  
  /**
   * Returns CodeListApi
   * 
   * @return CodeListApi
   */
  public CodeListApi getCodeListApi() {
    return new CodeListApi(client, null);
  }

  /**
   * Returns GeneralDescriptionApi
   * 
   * @param organizationId organization id
   * @return GeneralDescriptionApi
   */
  public GeneralDescriptionApi getGeneralDescriptionApi(OrganizationId organizationId) {
    return new GeneralDescriptionApi(client, getAccessToken(organizationId));
  }

  /**
   * Returns ServiceApi
   * 
   * @param organizationId organization id
   * @return ServiceApi
   */
  public ServiceApi getServiceApi(OrganizationId organizationId) {
    return new ServiceApi(client, getAccessToken(organizationId));
  }

  /**
   * Returns ServiceChannelApi
   * 
   * @param organizationId organization id
   * @return ServiceChannelApi
   */
  public ServiceChannelApi getServiceChannelApi(OrganizationId organizationId) {
    return new ServiceChannelApi(client, getAccessToken(organizationId));
  }
  
  /**
   * Returns ConnectionApi
   * 
   * @param organizationId organization id
   * @return ConnectionApi
   */
  public ConnectionApi getConnectionApi(OrganizationId organizationId) {
    return new ConnectionApi(client, getAccessToken(organizationId));
  }
  
  /**
   * Returns PTV access token for given organization
   * 
   * @param organizationId organization id
   * @return access token or null if not found
   */
  public String getAccessToken(OrganizationId organizationId) {
    if (organizationId == null) {
      return null;
    }
    
    return externalAccessTokenController.getOrganizationExternalAccessTokenValue(organizationId, PtvConsts.PTV_ACCESS_TOKEN_TYPE);
  }
}
