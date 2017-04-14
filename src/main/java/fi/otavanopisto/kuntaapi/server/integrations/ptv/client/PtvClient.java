package fi.otavanopisto.kuntaapi.server.integrations.ptv.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.ResultType;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;

/**
 * API Client for palvelutietovaranto
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvClient extends fi.metatavu.ptv.client.ApiClient {

  private static final String DEFAULT_PTV_URL = "https://api.palvelutietovaranto.trn.suomi.fi";
  private static final String INVALID_URI_SYNTAX = "Invalid uri syntax";
  
  @Inject
  private Logger logger;

  @Inject
  private GenericHttpClient httpClient;
  
  @Override
  public <T> ApiResponse<T> doGETRequest(String path, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(String.format("%s%s", getBasePath(), path));
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
  public <T> ApiResponse<T> doPOSTRequest(String path, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    return null;
  }

  @Override
  public <T> ApiResponse<T> doPUTRequest(String path, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    return null;
  }

  @Override
  public <T> ApiResponse<T> doDELETERequest(String path, ResultType<T> resultType, Map<String, Object> queryParams, Map<String, Object> postParams) {
    return null;
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
    if (value instanceof OffsetDateTime) {
      OffsetDateTime offsetDateTime = (OffsetDateTime) value;
      ZonedDateTime utcWithoutSeconds = offsetDateTime.atZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(utcWithoutSeconds);
    } 
    
    if (value instanceof LocalDateTime) {
      LocalDateTime localDateTime = (LocalDateTime) value;
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime.truncatedTo(ChronoUnit.SECONDS));
    } 
    
    return String.valueOf(value);
  }
  
  private String getBasePath() {
    String ptvUrl = System.getProperty("ptv.url");
    if (StringUtils.isNotBlank(ptvUrl)) {
      return ptvUrl;
    }
    
    return DEFAULT_PTV_URL;
  }
  
}
