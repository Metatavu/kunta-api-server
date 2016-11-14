package fi.otavanopisto.kuntaapi.server.integrations.management;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.MwpClient;
import fi.otavanopisto.kuntaapi.server.integrations.mwp.MwpConsts;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.mwp.client.DefaultApi;

/**
 * Api client for management service
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ManagementApi {
  
  @Inject
  private MwpClient client;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  /**
   * Returns management service API
   * 
   * @param organizationId management service organization id
   * @return management service API
   */
  public DefaultApi getApi(OrganizationId organizationId) {
    String basePath = organizationSettingController.getSettingValue(organizationId, MwpConsts.ORGANIZATION_SETTING_BASEURL);
    return new DefaultApi(basePath, client);
  }
  
}
