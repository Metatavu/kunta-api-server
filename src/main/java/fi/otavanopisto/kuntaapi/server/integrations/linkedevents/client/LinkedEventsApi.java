package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.linkedevents.client.EventApi;
import fi.metatavu.linkedevents.client.FilterApi;
import fi.metatavu.linkedevents.client.ImageApi;
import fi.metatavu.linkedevents.client.LanguageApi;
import fi.metatavu.linkedevents.client.SearchApi;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class LinkedEventsApi {
  
  @Inject
  private LinkedEventsClient client;

  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public EventApi getEventApi(OrganizationId organizationId) {
    return new EventApi(getBaseUrl(organizationId), client);
  }

  public FilterApi getFilterApi(OrganizationId organizationId) {
    return new FilterApi(getBaseUrl(organizationId), client);
  }

  public ImageApi getImageApi(OrganizationId organizationId) {
    return new ImageApi(getBaseUrl(organizationId), client);
  }

  public LanguageApi getLanguageApi(OrganizationId organizationId) {
    return new LanguageApi(getBaseUrl(organizationId), client);
  }

  public SearchApi getSearchApi(OrganizationId organizationId) {
    return new SearchApi(getBaseUrl(organizationId), client);
  }

  private String getBaseUrl(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL);
  }

}
