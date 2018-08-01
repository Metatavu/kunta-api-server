package fi.metatavu.kuntaapi.server.integrations.tpt.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.metatavu.kuntaapi.server.integrations.tpt.TptConsts;
import fi.metatavu.kuntaapi.server.integrations.tpt.client.model.ApiResponse;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

/**
 * API client for te-palvelut.fi -service
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptApi {
  
  @Inject
  private Logger logger;

  @Inject
  private GenericHttpClient httpClient;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public Response<ApiResponse> searchByArea(OrganizationId organizationId) {
    String area;
    try {
      area = getArea(organizationId);
    } catch (UnsupportedEncodingException e) {
      return new Response<>(500, e.getMessage(), null);
    }
    
    if (area == null) {
      return null;
    }
    
    String uri;
    try {
      String path = String.format(TptConsts.AREA_SEARCH_PATH, URLEncoder.encode(area, "UTF-8"));
      uri = String.format("%s%s", getBaseUrl(), path);
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.SEVERE, "Could not encode area parameter", e);
      return null;
    }
    
    return httpClient.doGETRequest(URI.create(uri), new GenericHttpClient.ResultType<ApiResponse>() {});
  }
  
  /**
   * Returns base URL for the te-palvelut.fi -service API
   * 
   * @return base URL for the te-palvelut.fi -service API
   */
  public String getBaseUrl() {
    return systemSettingController.getSettingValue(TptConsts.SYSTEM_SETTING_BASE_URL, TptConsts.DEFAULT_BASE_URL);
  }
  
  /**
   * Returns tpt area for for given organization
   * 
   * @param organizationId management service organization id
   * @return tpt area for for given organization
   * @throws UnsupportedEncodingException 
   */
  private String getArea(OrganizationId organizationId) throws UnsupportedEncodingException {
    return URLDecoder.decode(organizationSettingController.getSettingValue(organizationId, TptConsts.ORGANIZATION_SETTING_AREA), "UTF-8");
  }
  
}
