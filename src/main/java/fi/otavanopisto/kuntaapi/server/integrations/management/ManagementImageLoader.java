package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;

@ApplicationScoped
public class ManagementImageLoader {

  @Inject
  private Logger logger;
  
  @Inject
  private BinaryHttpClient binaryHttpClient;

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
      logger.severe(String.format("Image download failed on [%d] %s", response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  public Integer getImageSize(String url) {
    return binaryHttpClient.getDownloadSize(url);
  }
  
}
