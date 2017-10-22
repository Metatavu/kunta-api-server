package fi.otavanopisto.kuntaapi.server.integrations.ptv.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.GeneralDescriptionApi;
import fi.metatavu.ptv.client.OrganizationApi;
import fi.metatavu.ptv.client.ServiceApi;
import fi.metatavu.ptv.client.ServiceChannelApi;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.security.ExternalAccessTokenController;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PtvApi {
  
  @Inject
  private PtvClient client;

  @Inject
  private ExternalAccessTokenController externalAccessTokenController;
  
  public OrganizationApi getOrganizationApi() {
    return new OrganizationApi(client, null);
  }

  public GeneralDescriptionApi getGeneralDescriptionApi(OrganizationId organizationId) {
    return new GeneralDescriptionApi(client, getAccessToken(organizationId));
  }

  public ServiceApi getServiceApi(OrganizationId organizationId) {
    return new ServiceApi(client, getAccessToken(organizationId));
  }

  public ServiceChannelApi getServiceChannelApi(OrganizationId organizationId) {
    return new ServiceChannelApi(client, getAccessToken(organizationId));
  }
  
  public String getAccessToken(OrganizationId organizationId) {
    if (organizationId == null) {
      return null;
    }
    
    return externalAccessTokenController.getOrganizationExternalAccessTokenValue(organizationId, PtvConsts.PTV_ACCESS_TOKEN_TYPE);
  }
}
