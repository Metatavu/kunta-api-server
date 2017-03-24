package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.restfulptv.client.ElectronicServiceChannelsApi;
import fi.metatavu.restfulptv.client.OrganizationServicesApi;
import fi.metatavu.restfulptv.client.OrganizationsApi;
import fi.metatavu.restfulptv.client.PhoneServiceChannelsApi;
import fi.metatavu.restfulptv.client.PrintableFormServiceChannelsApi;
import fi.metatavu.restfulptv.client.ServiceLocationServiceChannelsApi;
import fi.metatavu.restfulptv.client.ServicesApi;
import fi.metatavu.restfulptv.client.StatutoryDescriptionsApi;
import fi.metatavu.restfulptv.client.WebPageServiceChannelsApi;

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

  public WebPageServiceChannelsApi getWebPageServiceChannelsApi() {
    return new WebPageServiceChannelsApi(getBaseUrl(), client);
  }

  public ServiceLocationServiceChannelsApi getServiceLocationServiceChannelsApi() {
    return new ServiceLocationServiceChannelsApi(getBaseUrl(), client);
  }

  public PrintableFormServiceChannelsApi getPrintableFormServiceChannelsApi() {
    return new PrintableFormServiceChannelsApi(getBaseUrl(), client);
  }

  public PhoneServiceChannelsApi getPhoneServiceChannelsApi() {
    return new PhoneServiceChannelsApi(getBaseUrl(), client);
  }

  public OrganizationServicesApi getOrganizationServicesApi() {
    return new OrganizationServicesApi(getBaseUrl(), client);
  }

  public ElectronicServiceChannelsApi getElectronicServiceChannelsApi() {
    return new ElectronicServiceChannelsApi(getBaseUrl(), client);
  }
  
  private String getBaseUrl() {
    return systemSettingController.getSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
}
