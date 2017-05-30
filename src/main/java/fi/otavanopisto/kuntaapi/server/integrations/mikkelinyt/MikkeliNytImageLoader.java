package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
public class MikkeliNytImageLoader {

  @Inject
  private Logger logger;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;

  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public AttachmentData getImageData(OrganizationId organizationId, AttachmentId mikkeliNytId) {
    String imageBaseUrl = organizationSettingController.getSettingValue(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_IMAGEBASEURL);
    if (StringUtils.isNotBlank(imageBaseUrl)) {
      String imageUrl = String.format("%s%s", imageBaseUrl, mikkeliNytId.getId());
      return getImageData(imageUrl);
    }
    
    logger.severe(String.format("Image imageBaseUrl has not been configured properly for organization %s", organizationId));
    
    return null;
  }

  public AttachmentData getImageData(String imageUrl) {
    URI uri;
    
    try {
      uri = new URIBuilder(imageUrl).build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, String.format("Invalid uri %s", imageUrl), e);
      return null;
    }
    
    return getImageData(uri);
  }

  public AttachmentData getImageData(URI uri) {
    Response<BinaryResponse> response = binaryHttpClient.downloadBinary(uri);
    if (response.isOk()) {
      return new AttachmentData(response.getResponseEntity().getType(), response.getResponseEntity().getData());
    } else {
      logger.severe(String.format("Image download failed from %s on [%d] %s", uri.toString(), response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  public Integer getImageSize(String url) {
    DownloadMeta downloadMeta = binaryHttpClient.getDownloadMeta(url);
    if (downloadMeta != null) {
      return downloadMeta.getSize();
    }
    
    return null;
  }
  
}
