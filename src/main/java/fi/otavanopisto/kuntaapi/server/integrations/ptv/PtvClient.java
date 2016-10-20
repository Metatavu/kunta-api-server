package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.ResultType;

/**
 * API Client for RESTful PTV Server
 * 
 * @author Antti Leppä
 */
@Dependent
public class PtvClient implements fi.otavanopisto.restfulptv.client.ApiClient {

  private static final String INVALID_URI_SYNTAX = "Invalid uri syntax";
  
  @Inject
  private Logger logger;

  @Inject
  private GenericHttpClient httpClient;

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
        addQueryParam(uriBuilder, entry);
      }
    }
    
    URI uri;
    try {
      uri = uriBuilder.build();
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new ApiResponse<>(500, INVALID_URI_SYNTAX, null);
    }
    
    Response<T> response = httpClient.doGETRequest(uri, new GenericHttpClient.ResultTypeWrapper<>(resultType.getType()));
    return new ApiResponse<>(response.getStatus(), response.getMessage(), response.getResponseEntity());
  }

  @Override
  public <T> ApiResponse<T> doPOSTRequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    throw new UnsupportedOperationException("PtvClient does not support post requests yet");
  }

  @Override
  public <T> ApiResponse<T> doPUTRequest(String url, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    throw new UnsupportedOperationException("PtvClient does not support put requests yet");
  }
  
  private void addQueryParam(URIBuilder uriBuilder, Entry<String, Object> entry) {
    if (entry.getValue() instanceof List) {
      for (Object value : (List<?>) entry.getValue()) {
        uriBuilder.addParameter(entry.getKey(), parameterToString(value));
      }
    } else {
      uriBuilder.addParameter(entry.getKey(), parameterToString(entry.getValue()));
    }
  }
  
  private String parameterToString(Object value) {
    return String.valueOf(value);
  }

}
