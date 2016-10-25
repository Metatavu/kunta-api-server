package fi.otavanopisto.kuntaapi.server.integrations.casem;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.casem.client.ContentLanguagesApi;
import fi.otavanopisto.casem.client.ContentsApi;
import fi.otavanopisto.casem.client.FilesApi;
import fi.otavanopisto.casem.client.NodesApi;
import fi.otavanopisto.casem.client.TemplatesApi;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

/**
 * Api client for Case M
 * 
 * @author Antti Leppä
 */
@Dependent
public class CaseMApi {

  @Inject
  private CaseMClient client;

  @Inject
  private OrganizationSettingController organizationSettingController;

  private CaseMApi() {
  }

  /**
   * Returns content languages api
   * 
   * @param organizationId organization id
   * @return content languages api
   */
  public ContentLanguagesApi getContentLanguagesApi(OrganizationId organizationId) {
    return new ContentLanguagesApi(getBaseUrl(organizationId), client);
  }

  /**
   * Returns content api
   * 
   * @param organizationId organization id
   * @return content api
   */
  public ContentsApi getContentsApi(OrganizationId organizationId) {
    return new ContentsApi(getBaseUrl(organizationId), client);
  }

  /**
   * Returns files api
   * 
   * @param organizationId organization id
   * @return content files api
   */  
  public FilesApi getFilesApi(OrganizationId organizationId) {
    return new FilesApi(getBaseUrl(organizationId), client);
  }

  /**
   * Returns nodes api
   * 
   * @param organizationId organization id
   * @return nodes api
   */  
  public NodesApi getNodesApi(OrganizationId organizationId) {
    return new NodesApi(getBaseUrl(organizationId), client);
  }

  /**
   * Returns templates api
   * 
   * @param organizationId organization id
   * @return templates api
   */  
  public TemplatesApi getTemplatesApi(OrganizationId organizationId) {
    return new TemplatesApi(getBaseUrl(organizationId), client);
  }

  private String getBaseUrl(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_BASEURL) + "/api/opennc/v1";
  }

}
