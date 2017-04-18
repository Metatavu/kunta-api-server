package fi.otavanopisto.kuntaapi.server.integrations.ptv.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.GeneralDescriptionApi;
import fi.metatavu.ptv.client.OrganizationApi;
import fi.metatavu.ptv.client.ServiceApi;
import fi.metatavu.ptv.client.ServiceChannelApi;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PtvApi {
  
  @Inject
  private PtvClient client;
  
  public OrganizationApi getOrganizationApi() {
    return new OrganizationApi(client);
  }

  public GeneralDescriptionApi getGeneralDescriptionApi() {
    return new GeneralDescriptionApi(client);
  }

  public ServiceApi getServiceApi() {
    return new ServiceApi(client);
  }

  public ServiceChannelApi getServiceChannelApi() {
    return new ServiceChannelApi(client);
  }
}
