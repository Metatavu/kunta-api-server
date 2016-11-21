package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
public class CaseMFileController {
  
  @Inject
  private Logger logger;

  @Inject
  private BinaryHttpClient binaryHttpClient;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public BinaryResponse downloadFile(FileId caseMFileId) {
    String downloadUrl = getDownloadUrl(caseMFileId);
    Response<BinaryResponse> response = binaryHttpClient.downloadBinary(downloadUrl);
    if (response.isOk()) {
      return response.getResponseEntity();
    } else {
      logger.warning(String.format("Downloading attachment from %s failed on [%d] %s", downloadUrl, response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  public DownloadMeta getDownloadMeta(FileId caseMFileId) {
    String downloadUrl = getDownloadUrl(caseMFileId);
    return binaryHttpClient.getDownloadMeta(downloadUrl);
  }
  
  private String getDownloadUrl(FileId caseMFileId) {
    OrganizationId organizationId = caseMFileId.getOrganizationId();
    String downloadUrl = getCaseMDownloadUrl(organizationId);
    try {
      return String.format("%s/%s", downloadUrl, URLEncoder.encode(caseMFileId.getId(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.SEVERE, "Failed to encode url", e);
    }
    
    return null;
  }
  
  private String getCaseMDownloadUrl(OrganizationId organizationId) {
    String baseUrl = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_BASEURL);
    String path = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_DOWNLOAD_PATH);
    return String.format("%s/%s", baseUrl, path);
  }
}
