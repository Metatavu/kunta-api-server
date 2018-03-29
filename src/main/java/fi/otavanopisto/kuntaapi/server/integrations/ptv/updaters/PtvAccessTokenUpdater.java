package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.ResultType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.AccessTokenTaskQueue;
import fi.otavanopisto.kuntaapi.server.security.ExternalAccessTokenController;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvAccessTokenUpdater extends EntityUpdater<OrganizationEntityUpdateTask> {
  
  private static final long TOKEN_EXPIRE_MAX_TIME = 60l * 60 * 24 * 7;

  @Inject
  private Logger logger;

  @Inject
  private ExternalAccessTokenController externalAccessTokenController;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private AccessTokenTaskQueue accessTokenTaskQueue;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private GenericHttpClient genericHttpClient;
  
  @Override
  public String getName() {
    return "PTV.accessToken";
  }
  
  @Override
  public void execute(OrganizationEntityUpdateTask task) {
    refreshAccessToken(task.getOrganizationId());
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = accessTokenTaskQueue.next();
      if (task != null) {
        execute(task);
      } else {
        if (accessTokenTaskQueue.isEmptyAndLocalNodeResponsible()) {
          accessTokenTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(PtvConsts.ORGANIZATION_SETTING_API_PASS));
        }
      }
    }
  }
  
  @SuppressWarnings ("squid:S2068")
  private void refreshAccessToken(OrganizationId kuntaApiOrganizationId) {
    OffsetDateTime expires = externalAccessTokenController.getOrganizationExternalAccessTokenExpires(kuntaApiOrganizationId, PtvConsts.PTV_ACCESS_TOKEN_TYPE);
    OffsetDateTime refreshTime = OffsetDateTime.now().plusHours(6);
    
    if (expires != null && !refreshTime.isAfter(expires)) {
      return;
    }
    
    String baseUrl = systemSettingController.getSettingValue(PtvConsts.SYSTEM_SETTING_STS_BASEURL);
    String apiUser = organizationSettingController.getSettingValue(kuntaApiOrganizationId, PtvConsts.ORGANIZATION_SETTING_API_USER);
    String apiPass = organizationSettingController.getSettingValue(kuntaApiOrganizationId, PtvConsts.ORGANIZATION_SETTING_API_PASS);
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    URI uri = null;
    try {
      uri = new URI(String.format("%s/connect/token", baseUrl));
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, "Failed to construct PTV access token url", e);
      return;
    }
    
    try {
      UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(Arrays.asList(
        new BasicNameValuePair("grant_type", "password"),
        new BasicNameValuePair("scope", "dataEventRecords openid"),
        new BasicNameValuePair("client_id", "ptv_api_client"),
        new BasicNameValuePair("client_secret", "openapi"),
        new BasicNameValuePair("username", apiUser),
        new BasicNameValuePair("password", apiPass)
      ));
      
      ResultType<PtvAccessToken> resultType = new GenericHttpClient.ResultType<PtvAccessToken>() {};
      Response<PtvAccessToken> response = genericHttpClient.doPOSTRequest(uri, resultType, headers, formEntity);
      if (response.isOk()) {
        PtvAccessToken accessToken = response.getResponseEntity();
        Long expiresIn = accessToken.getExpiresIn();
        if (expiresIn == null) {
          expiresIn = TOKEN_EXPIRE_MAX_TIME;
        }
        
        OffsetDateTime tokenExpires = OffsetDateTime.now().plusSeconds(expiresIn);
        externalAccessTokenController.setOrganizationExternalAccessToken(kuntaApiOrganizationId, PtvConsts.PTV_ACCESS_TOKEN_TYPE, accessToken.getAccessToken(), tokenExpires);
      } else {
        logger.log(Level.SEVERE, () -> String.format("Failed to refresh PTV access token for organization %s: [%d]: %s", kuntaApiOrganizationId, response.getStatus(), response.getMessage())); 
      }
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.SEVERE, "Failed to construct PTV access token body", e);
      return;
    }
  }
  
  @JsonIgnoreProperties (ignoreUnknown = true)
  public static class PtvAccessToken {
    
    @JsonProperty (value = "access_token")
    private String accessToken;
    
    @JsonProperty (value = "expires_in")
    private Long expiresIn;
    
    @JsonProperty (value = "token_type")
    private String tokenType;
    
    public String getAccessToken() {
      return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }
    
    public Long getExpiresIn() {
      return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
      this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
      return tokenType;
    }
    
    public void setTokenType(String tokenType) {
      this.tokenType = tokenType;
    }
    
  }

}
