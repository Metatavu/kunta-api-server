package fi.metatavu.kuntaapi.server.integrations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import fi.metatavu.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.metatavu.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;

public abstract class AbstractImageLoader {
  
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
      logger.log(Level.SEVERE, () -> String.format("Image download failed from %s on [%d] %s", uri.toString(), response.getStatus(), response.getMessage()));
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
