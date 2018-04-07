package fi.otavanopisto.kuntaapi.server.integrations.management.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.client.model.PostMenuOrder;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.ResultType;

/**
 * API client for management service
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ManagementApi {
  
  @Inject
  private ManagementClient client;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  /**
   * Returns management service API
   * 
   * @param organizationId management service organization id
   * @return management service API
   */
  public DefaultApi getApi(OrganizationId organizationId) {
    return new DefaultApi(getBasePath(organizationId), client);
  }
  
  /**
   * Returns management service API base path for given organization
   * 
   * @param organizationId management service organization id
   * @return management service API base path for given organization
   */
  public String getBasePath(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
  /**
   * Returns post menu order number. If post menu order can not be resolved 0 is returned
   * 
   * @param organizationId management service organization id
   * @param postId post id
   * @return post order index
   */
  public Integer getPostMenuOrder(OrganizationId organizationId, Integer postId) {
    return getPostMenuOrder(organizationId, postId, 0);
  }
  
  /**
   * Returns post menu order number. If post menu order can not be resolved specified defaultOrder is returned
   * 
   * @param organizationId management service organization id
   * @param postId post id
   * @param defaultOrder default order value
   * @return post order index
   */
  public Integer getPostMenuOrder(OrganizationId organizationId, Integer postId, Integer defaultOrder) {
    ApiResponse<PostMenuOrder> response = getPostMenuOrderRequest(organizationId, postId);
    if (response.isOk()) {
      Integer menuOrder = response.getResponse().getMenuOrder();
      if (menuOrder != null) {
        return menuOrder;
      }
    }
    
    return defaultOrder;
  }
  
  /**
   * Returns response from post menu order endpoint
   * 
   * @param organizationId management service organization id
   * @param postId post id
   * @return API response for request
   */
  public ApiResponse<PostMenuOrder> getPostMenuOrderRequest(OrganizationId organizationId, Integer postId) {
    String url = String.format("%s/menuorder/v1/posts/%d", getBasePath(organizationId), postId);
    ResultType<PostMenuOrder> resultType = new ResultType<PostMenuOrder>() {};
    return client.doGETRequest(url, resultType, null, null);
  }
  
  
}
