package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.restfulptv.client.ElectronicChannelsApi;
import fi.otavanopisto.restfulptv.client.OrganizationServicesApi;
import fi.otavanopisto.restfulptv.client.OrganizationsApi;
import fi.otavanopisto.restfulptv.client.PhoneChannelsApi;
import fi.otavanopisto.restfulptv.client.PrintableFormChannelsApi;
import fi.otavanopisto.restfulptv.client.ServiceLocationChannelsApi;
import fi.otavanopisto.restfulptv.client.ServicesApi;
import fi.otavanopisto.restfulptv.client.StatutoryDescriptionsApi;
import fi.otavanopisto.restfulptv.client.WebPageChannelsApi;

@Dependent
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
