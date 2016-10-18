package fi.otavanopisto.kuntaapi.server.integrations.mwp;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.mwp.client.DefaultApi;

/**
 * Api client for management service
 * 
 * @author Antti Leppä
 */
@Dependent
public class MwpApi {
  
  @Inject
  private MwpClient client;
  
  @Inject
  private OrganizationSettingController  organizationSettingController;
  
  private MwpApi() {
  }
  
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
