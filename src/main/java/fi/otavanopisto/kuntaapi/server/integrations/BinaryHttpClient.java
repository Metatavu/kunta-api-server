package fi.otavanopisto.kuntaapi.server.integrations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;

/**
 * HTTP client for downloading binaries
 * 
 * @author Antti Leppä
 */
@Dependent
public class BinaryHttpClient {

  @Inject
  private Logger logger;

  private BinaryHttpClient() {
  }
  
  /**
   * Downloads binary data
   * 
   * @param url url
   * @return binary response with http status
   */
  public Response<BinaryResponse> downloadBinary(String url) {
    URI uri;
    
    try {
      uri = new URIBuilder(url).build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, String.format("Invalid uri %s", url), e);
      return new Response<>(400, String.format("Malformed url address %s", url), null);
    }
    
    return downloadBinary(uri);
  }

  /**
   * Downloads binary data
   * 
   * @param uri uri
   * @return binary response with http status
   */
  public Response<BinaryResponse> downloadBinary(URI uri) {
    try {
      CloseableHttpClient client = HttpClients.createDefault();
      try {
        HttpGet httpGet = new HttpGet(uri);
        
        CloseableHttpResponse httpResponse = client.execute(httpGet);
        try {
          StatusLine statusLine = httpResponse.getStatusLine();
          int statusCode = statusLine.getStatusCode();
          String message = statusLine.getReasonPhrase();
          byte[] data = IOUtils.toByteArray(httpResponse.getEntity().getContent());
          Header typeHeader = httpResponse.getEntity().getContentType();
          String type = typeHeader != null ? typeHeader.getValue() : null;
          return new Response<>(statusCode, message, new BinaryResponse(type, data));
        } finally {
          httpResponse.close();
        }
      } finally {
        client.close();
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, String.format("Failed to fetch binary data from %s", uri.toString()), e);
      return new Response<>(500, "Interval Server Error", null);
    }
  }
  
  /**
   * Resolve download meta data
   * 
   * @param url URL
   * @return download meta data or null on failure
   */
  public DownloadMeta getDownloadMeta(String url) {
    URI uri;
    
    try {
      uri = new URIBuilder(url).build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, String.format("Invalid uri %s", url), e);
      return null;
    }
    
    return getDownloadMeta(uri);
  }
  
  /**
   * Resolve download meta data
   * 
   * @param uri URI
   * @return download meta data or null on failure
   */
  public DownloadMeta getDownloadMeta(URI uri) {
    try {
      CloseableHttpClient client = HttpClients.createDefault();
      try {
        HttpHead httpHead = new HttpHead(uri);
        
        CloseableHttpResponse httpResponse = client.execute(httpHead);
        try {
          Integer size = getIntegerHeader(httpResponse, "Content-Length");
          String contentType = getStringHeader(httpResponse, "Content-Type");
          return new DownloadMeta(size, contentType);
        } finally {
          httpResponse.close();
        }
      } finally {
        client.close();
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, String.format("Failed to from %s", uri.toString()), e);
    }
    
    return null;
  }
  
  private Integer getIntegerHeader(HttpResponse httpResponse, String name) {
    Header header = httpResponse.getFirstHeader(name);
    if (header != null) {
      String value = header.getValue();
      if (StringUtils.isNumeric(value)) {
        return NumberUtils.createInteger(value);
      }
    }
    
    return null;
  }
  
  private String getStringHeader(HttpResponse httpResponse, String name) {
    Header header = httpResponse.getFirstHeader(name);
    if (header != null) {
      return header.getValue();
    }
    
    return null;
  }
  
  /**
   * Class representing binary response
   * 
   * @author Antti Leppä
   */
  public class BinaryResponse {
    
    private byte[] data;
    private String type;
    
    /**
     * Constructor for class
     * 
     * @param type content type 
     * @param data data
     */
    public BinaryResponse(String type, byte[] data) {
      this.data = data;
      this.type = type;
    }
    
    public byte[] getData() {
      return data;
    }
    
    public String getType() {
      return type;
    }
    
  }
  
  public class DownloadMeta {
    
    private Integer size;
    private String contentType;
    
    public DownloadMeta(Integer size, String contentType) {
      this.size = size;
      this.contentType = contentType;
    }
    
    public String getContentType() {
      return contentType;
    }
    
    public Integer getSize() {
      return size;
    }
    
  }
  
}
