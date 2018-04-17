package fi.otavanopisto.kuntaapi.server.integrations.tpt.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.TptConsts;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.ApiResponse;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

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
  private OrganizationSettingController organizationSettingController;
  
  public Response<ApiResponse> searchByArea(OrganizationId organizationId) {
    String area = getArea(organizationId);
    if (area == null) {
      return null;
    }
    
    String uri;
    try {
      uri = String.format(TptConsts.AREA_SEARCH_URL, URLEncoder.encode(area, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.SEVERE, "Could not encode area parameter", e);
      return null;
    }
    
    return httpClient.doGETRequest(URI.create(uri), new GenericHttpClient.ResultType<ApiResponse>() {});
  }
  
  /**
   * Returns tpt area for for given organization
   * 
   * @param organizationId management service organization id
   * @return tpt area for for given organization
   */
  public String getArea(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, TptConsts.ORGANIZATION_AREA);
  }
  
}
