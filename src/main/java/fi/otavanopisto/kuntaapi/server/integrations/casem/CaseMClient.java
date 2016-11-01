package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
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
import fi.otavanopisto.casem.client.ApiResponse;

/**
 * API Client for CaseM
 * 
 * @author Antti Leppä
 */
@Dependent
public class CaseMClient implements fi.otavanopisto.casem.client.ApiClient {

  private static final String INVALID_URI_SYNTAX = "Invalid uri syntax";

  @Inject
  private Logger logger;

  @Inject
  private GenericHttpClient httpClient;

  @Inject
  private GenericHttpCache httpCache;
  
  private CaseMClient() {
  }
  
  @Override
  public <T> ApiResponse<T> doGETRequest(String url, fi.otavanopisto.casem.client.ResultType<T> resultType, Map<String, Object> queryParams) {
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
    
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    
    Response<T> response = httpCache.get(CaseMConsts.CACHE_NAME, uri, new GenericHttpClient.ResponseResultTypeWrapper<>(resultType.getType()));
    if (response == null) {
      response = httpClient.doGETRequest(uri, new GenericHttpClient.ResultTypeWrapper<>(resultType.getType()), headers);
      if (CaseMConsts.CACHE_RESPONSES) {
        httpCache.put(CaseMConsts.CACHE_NAME, uri, response);
      }
    }
    
    return new ApiResponse<>(response.getStatus(), response.getMessage(), response.getResponseEntity());
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
