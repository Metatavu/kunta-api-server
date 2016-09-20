package fi.otavanopisto.kuntaapi.server.integrations.mwp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpCache;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.ResultType;

/**
 * API Client for management service
 * 
 * @author Antti Leppä
 */
@Dependent
public class MwpClient implements fi.otavanopisto.mwp.client.ApiClient {

  private static final String INVALID_URI_SYNTAX = "Invalid uri syntax";

  @Inject
  private Logger logger;

  @Inject
  private GenericHttpClient httpClient;

  @Inject
  private GenericHttpCache httpCache;
  
  private MwpClient() {
  }
  
  @Override
  public <T> ApiResponse<T> doGETRequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(url);
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new ApiResponse<>(500, INVALID_URI_SYNTAX, null);
    }
    
    if (queryParams != null) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        uriBuilder.addParameter(entry.getKey(), parameterToString(entry.getValue()));
      }
    }
    
    URI uri;
    try {
      uri = uriBuilder.build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new ApiResponse<>(500, INVALID_URI_SYNTAX, null);
    }
    
    Response<T> response = httpCache.get(MwpConsts.CACHE_NAME, uri, new GenericHttpClient.ResponseResultTypeWrapper<>(resultType.getType()));
    if (response == null) {
      response = httpClient.doGETRequest(uri, new GenericHttpClient.ResultTypeWrapper<>(resultType.getType()));
      if (MwpConsts.CACHE_RESPONSES) {
        httpCache.put(MwpConsts.CACHE_NAME, uri, response);
      }
    }
    
    return new ApiResponse<>(response.getStatus(), response.getMessage(), response.getResponseEntity());
  }

  @Override
  public <T> ApiResponse<T> doPOSTRequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> ApiResponse<T> doPUTRequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> ApiResponse<T> doDELETERequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    throw new UnsupportedOperationException();
  }
  
  private String parameterToString(Object value) {
    return String.valueOf(value);
  }
  
}
