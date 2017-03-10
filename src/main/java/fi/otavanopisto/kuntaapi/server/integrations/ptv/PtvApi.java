package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.restfulptv.client.ElectronicChannelsApi;
import fi.metatavu.restfulptv.client.OrganizationServicesApi;
import fi.metatavu.restfulptv.client.OrganizationsApi;
import fi.metatavu.restfulptv.client.PhoneChannelsApi;
import fi.metatavu.restfulptv.client.PrintableFormChannelsApi;
import fi.metatavu.restfulptv.client.ServiceLocationChannelsApi;
import fi.metatavu.restfulptv.client.ServicesApi;
import fi.metatavu.restfulptv.client.StatutoryDescriptionsApi;
import fi.metatavu.restfulptv.client.WebPageChannelsApi;

@ApplicationScoped
public class PtvApi {
  
  @Inject
  private PtvClient client;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  public OrganizationsApi getOrganizationApi() {
    return new OrganizationsApi(getBaseUrl(), client);
  }

  public StatutoryDescriptionsApi getStatutoryDescriptionsApi() {
    return new StatutoryDescriptionsApi(getBaseUrl(), client);
  }

  public ServicesApi getServicesApi() {
    return new ServicesApi(getBaseUrl(), client);
  }

  public WebPageChannelsApi getWebPageChannelsApi() {
    return new WebPageChannelsApi(getBaseUrl(), client);
  }

  public ServiceLocationChannelsApi getServiceLocationChannelsApi() {
    return new ServiceLocationChannelsApi(getBaseUrl(), client);
  }

  public PrintableFormChannelsApi getPrintableFormChannelsApi() {
    return new PrintableFormChannelsApi(getBaseUrl(), client);
  }

  public PhoneChannelsApi getPhoneChannelsApi() {
    return new PhoneChannelsApi(getBaseUrl(), client);
  }

  public OrganizationServicesApi getOrganizationServicesApi() {
    return new OrganizationServicesApi(getBaseUrl(), client);
  }

  public ElectronicChannelsApi getElectronicChannelsApi() {
    return new ElectronicChannelsApi(getBaseUrl(), client);
  }
  
  private String getBaseUrl() {
    return systemSettingController.getSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
}
