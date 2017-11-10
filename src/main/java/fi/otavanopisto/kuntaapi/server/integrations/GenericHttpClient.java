package fi.otavanopisto.kuntaapi.server.integrations;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Response aware HTTP client for integrations
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class GenericHttpClient {

  private static final String MALFORMED_ADDRESS = "Malformed address %s";
  private static final String REQUEST_TIMED_OUT = "Request timed out";
  private static final String RESPONSE_PARSING_FAILED = "Response parsing failed";
  private static final String INVALID_URI_SYNTAX = "Invalid uri syntax";
  private static final String FAILED_TO_SERIALIZE_BODY = "Failed to serialize body";
  
  @Inject
  private Logger logger;
  
  private List<Module> modules;

  private GenericHttpClient() {
  }
  
  public void setModules(List<Module> modules) {
    this.modules = modules;
  }
  
  /**
   * Executes a HEAD request into a specified URI
   * 
   * @param basePath base path for all requests
   * @param path path of request
   * @param resultType result type
   * @param queryParams query params
   * @return the response
   */
  public <T> Response<T> doHEADRequest(String basePath, String path, ResultType<T> resultType, Map<String, Object> queryParams) {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(String.format("%s%s", basePath, path));
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
    
    if (queryParams != null) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        uriBuilder.addParameter(entry.getKey(), parameterToString(entry.getValue()));
      }
    }
    
    try {
      return doHEADRequest(uriBuilder.build(), resultType, null);
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
  }
  
   /**
   * Executes a HEAD request into a specified URI
   * 
   * @param uri request uri
   * @param resultType result type
   * @param extraHeaders extra headers for the request
   * @return the response
   */
  public <T> Response<T> doHEADRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders) {
    if (StringUtils.isBlank(uri.getHost())) {
      return new Response<>(400, String.format(MALFORMED_ADDRESS, uri), null);
    }
    
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      return executeHEADRequest(resultType, uri, httpClient, extraHeaders, null);
    } finally {
      closeClient(httpClient);
    }
  }
  
  /**
   * Executes a get request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @return the response
   */
  public <T> Response<T> doHEADRequest(URI uri, ResultType<T> resultType) {
    return doHEADRequest(uri, resultType, null);
  }
  
  /**
   * Executes a get request into a specified URI
   * 
   * @param basePath base path for all requests
   * @param path path of request
   * @param resultType type of request
   * @param queryParams query params
   * @return the response
   */
  public <T> Response<T> doGETRequest(String basePath, String path, ResultType<T> resultType, Map<String, Object> queryParams) {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(String.format("%s%s", basePath, path));
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
    
    if (queryParams != null) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        uriBuilder.addParameter(entry.getKey(), parameterToString(entry.getValue()));
      }
    }
    
    try {
      return doGETRequest(uriBuilder.build(), resultType, null, null);
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
  }
  
  /**
   * Executes a put request into a specified URI
   * 
   * @param basePath base path for all requests
   * @param path path of request
   * @param resultType type of request
   * @param queryParams query params
   * @param body body
   * @return the response
   */
  public <T> Response<T> doPUTRequest(String basePath, String path, ResultType<T> resultType, Map<String, Object> queryParams, String body) {
    return doPUTRequest(basePath, path, resultType, queryParams, body, null);
  }
  
  /**
   * Executes a put request into a specified URI
   * 
   * @param basePath base path for all requests
   * @param path path of request
   * @param resultType type of request
   * @param queryParams query params
   * @param body body
   * @param accessToken access token (optional)
   * @return the response
   */
  public <T> Response<T> doPUTRequest(String basePath, String path, ResultType<T> resultType, Map<String, Object> queryParams, String body, String accessToken) {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(String.format("%s%s", basePath, path));
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
    
    if (queryParams != null) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        uriBuilder.addParameter(entry.getKey(), parameterToString(entry.getValue()));
      }
    }
    
    try {
      return doPUTRequest(uriBuilder.build(), resultType, null, body, accessToken);
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, INVALID_URI_SYNTAX, e);
      return new Response<>(500, INVALID_URI_SYNTAX, null);
    }
  }
  
  /**
   * Executes a get request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @return the response
   */
  public <T> Response<T> doGETRequest(URI uri, ResultType<T> resultType) {
    return doGETRequest(uri, resultType, null, null);
  }

  /**
   * Executes a put request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @param body body
   * @param accessToken access token (optional)
   * @return the response
   */
  public <T> Response<T> doPUTRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders, Object body, String accessToken) {
    if (StringUtils.isBlank(uri.getHost())) {
      return new Response<>(400, String.format(MALFORMED_ADDRESS, uri), null);
    }
    
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      return executePUTRequest(resultType, uri, httpClient, extraHeaders, body, accessToken);
    } finally {
      closeClient(httpClient);
    }
  }
  
  /**
   * Executes a post request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @param body body
   * @return the response
   */
  public <T> Response<T> doPOSTRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders, Object body) {
    return doPOSTRequest(uri, resultType, extraHeaders, body, null);
  }
  
  /**
   * Executes a post request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @param body body
   * @return the response
   */
  public <T> Response<T> doPOSTRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders, byte[] body) {
    return doPOSTRequest(uri, resultType, extraHeaders, body, null);
  }

  /**
   * Executes a post request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @param body body
   * @param accessToken access token (optional)
   * @return the response
   */
  public <T> Response<T> doPOSTRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders, Object body, String accessToken) {
    if (StringUtils.isBlank(uri.getHost())) {
      return new Response<>(400, String.format(MALFORMED_ADDRESS, uri), null);
    }
    
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      return executePOSTRequest(resultType, uri, httpClient, extraHeaders, body, accessToken);
    } finally {
      closeClient(httpClient);
    }
  }
  
  /**
   * Executes a get request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @return the response
   */
  public <T> Response<T> doGETRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders) {
    return doGETRequest(uri, resultType, extraHeaders, null);
  }

  /**
   * Executes a get request into a specified URI
   * 
   * @param uri request uri
   * @param resultType type of request
   * @param extraHeaders extra headers for the request
   * @param acessToken access token (optional)
   * @return the response
   */
  public <T> Response<T> doGETRequest(URI uri, ResultType<T> resultType, Map<String, String> extraHeaders, String acessToken) {
    if (StringUtils.isBlank(uri.getHost())) {
      return new Response<>(400, String.format(MALFORMED_ADDRESS, uri), null);
    }
    
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      return executeGETRequest(resultType, uri, httpClient, extraHeaders, acessToken);
    } finally {
      closeClient(httpClient);
    }
  }

  private <T> Response<T> executeGETRequest(ResultType<T> resultType, URI uri,
      CloseableHttpClient httpClient, Map<String, String> extraHeaders, String accessToken) {
    HttpGet httpGet = new HttpGet(uri);

    return executeRequest(httpClient, httpGet, resultType, extraHeaders, accessToken);
  }

  private <T> Response<T> executePUTRequest(ResultType<T> resultType, URI uri, CloseableHttpClient httpClient, Map<String, String> extraHeaders, Object body, String accessToken) {
    HttpPut httpPut = new HttpPut(uri);
    
    if (body instanceof HttpEntity) {
      httpPut.setEntity((HttpEntity) body);
    } else {
      try {
        byte[] bodyBytes = getJsonObjectMapper().writeValueAsBytes(body);
        httpPut.setEntity(new ByteArrayEntity(bodyBytes));
      } catch (IOException e) {
        logger.log(Level.SEVERE, FAILED_TO_SERIALIZE_BODY, e);
        return new Response<>(500, FAILED_TO_SERIALIZE_BODY, null);
      }
    }
    
    return executeRequest(httpClient, httpPut, resultType, extraHeaders, accessToken);
  }

  private <T> Response<T> executePOSTRequest(ResultType<T> resultType, URI uri, CloseableHttpClient httpClient, Map<String, String> extraHeaders, Object body, String accessToken) {
    HttpPost httpPost = new HttpPost(uri);

    if (body instanceof HttpEntity) {
      httpPost.setEntity((HttpEntity) body);
    } else {
      try {
        byte[] bodyBytes = getJsonObjectMapper().writeValueAsBytes(body);
        httpPost.setEntity(new ByteArrayEntity(bodyBytes));
      } catch (IOException e) {
        logger.log(Level.SEVERE, FAILED_TO_SERIALIZE_BODY, e);
        return new Response<>(500, FAILED_TO_SERIALIZE_BODY, null);
      }
    }
    
    return executeRequest(httpClient, httpPost, resultType, extraHeaders, accessToken);
  }
  
  private <T> Response<T> executeHEADRequest(ResultType<T> resultType, URI uri,
      CloseableHttpClient httpClient, Map<String, String> extraHeaders, String accessToken) {
    HttpHead httpHead = new HttpHead(uri);
    
    return executeRequest(httpClient, httpHead, resultType, extraHeaders, accessToken);
  }
  
  private <T> Response<T> executeRequest(CloseableHttpClient httpClient, HttpUriRequest request, ResultType<T> resultType, Map<String, String> extraHeaders, String accessToken) {
    
    if (extraHeaders != null) {
      for (Entry<String, String> extraHeader : extraHeaders.entrySet()) {
        request.addHeader(extraHeader.getKey(), extraHeader.getValue());
      }
    }
    
    if (accessToken != null) {
      request.addHeader("Authorization", String.format("Bearer %s", accessToken));
    }
    
    try {
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        return createResponse(response, request.getMethod(), resultType);
      }
    } catch (JsonParseException | JsonMappingException  e) {
      logger.log(Level.SEVERE, RESPONSE_PARSING_FAILED, e);
      return new Response<>(500, RESPONSE_PARSING_FAILED, null);
    } catch (IOException e) {
      logger.log(Level.SEVERE, REQUEST_TIMED_OUT, e);
      return new Response<>(408, REQUEST_TIMED_OUT, null);
    }
  }
  
  private void closeClient(CloseableHttpClient httpClient) {
    try {
      httpClient.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to close http client", e);
    }
  }

  private String parameterToString(Object value) {
    return String.valueOf(value);
  }
  
  private <T> Response<T> createResponse(HttpResponse httpResponse, String httpMethod, ResultType<T> resultType) throws IOException {
    StatusLine statusLine = httpResponse.getStatusLine();
    int statusCode = statusLine.getStatusCode();
    String message = statusLine.getReasonPhrase();
    
    switch (statusCode) {
      case 200:
        return httpMethod.equals(HttpHead.METHOD_NAME) ? handleNoContentResponse(statusCode, message, resultType.getTypeReference()) : handleOkResponse(httpResponse, statusCode, message, resultType.getTypeReference());
      case 204:
        return handleNoContentResponse(statusCode, message, resultType.getTypeReference());
      case 400:
        HttpEntity entity = httpResponse.getEntity();
        try (InputStream contentStream = entity.getContent()) {
          String content = IOUtils.toString(contentStream);
          if (StringUtils.isNotBlank(content)) {
            return handleErrorResponse(statusCode, content);
          }
        }
        
        return handleErrorResponse(statusCode, message);
      default:
        return handleErrorResponse(statusCode, message);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Response<T> handleOkResponse(HttpResponse httpResponse, int statusCode, String message, TypeReference<T> typeReference) throws IOException {
    HttpEntity entity = httpResponse.getEntity();
    try {
      String httpResponseContent = IOUtils.toString(entity.getContent());
      String contentType = getContentType(httpResponse);
      if ("text/xml".equals(contentType)) {
        XmlMapper xmlMapper = getXmlObjectMapper();
        return new Response<>(statusCode, message, (T) xmlMapper.readValue(httpResponseContent, typeReference));
      } else {
        ObjectMapper objectMapper = getJsonObjectMapper();
        return new Response<>(statusCode, message, (T) objectMapper.readValue(httpResponseContent, typeReference));
      }
    } finally {
      EntityUtils.consume(entity);
    }
  }

  private XmlMapper getXmlObjectMapper() {
    XmlMapper xmlMapper = new XmlMapper();
    registerModules(xmlMapper);
    return xmlMapper;
  }

  private ObjectMapper getJsonObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    registerModules(objectMapper);
    return objectMapper;
  }
  
  private void registerModules(ObjectMapper objectMapper) {
    if (modules != null) {
      for (Module module : modules) {
        objectMapper.registerModule(module);
      }
    } else {
      objectMapper.registerModule(new JavaTimeModule());
    }
  }

  private String getContentType(HttpResponse httpResponse) {
    Header header = httpResponse.getFirstHeader("Content-Type");
    if (header != null) {
      return header.getValue();
    }
    
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T> Response<T> handleNoContentResponse(int statusCode, String message, TypeReference<T> typeReference) {
    Type type = typeReference.getType();
    if (type instanceof Class) {
      Class<T> clazz = (Class<T>) type;
      if (clazz.isArray()) {
        return new Response<>(statusCode, message, (T) Array.newInstance(clazz.getComponentType(), 0));
      }
    }
    
    return new Response<>(statusCode, message, null);
  }

  private <T> Response<T> handleErrorResponse(int statusCode, String message) {
    return new Response<>(statusCode, message, null);
  }
  
  /**
   * Interface representing returned entity type
   * 
   * @author Otavan Opisto
   *
   * @param <T> type of returned entity
   */
  @SuppressWarnings ("squid:S2326")
  public static class ResultType<T> {
    
    /**
     * Returns generic type of result type
     * 
     * @return generic type of result type
     */
    public Type getType() {
      Type superClass = getClass().getGenericSuperclass();
      if (superClass instanceof ParameterizedType) {
        return ((ParameterizedType) superClass).getActualTypeArguments()[0];
      }
        
      return null;
    }
    
    public TypeReference<T> getTypeReference() {
      Type type = getType();
      return new TypeReference<T>() { 
        @Override
        public Type getType() {
          return type;
        }
      };
    }
  }
  
  /**
   * Class representing request response
   * 
   * @author Antti Leppä
   * @author Heikki Kurhinen
   *
   * @param <T> response type
   */
  public static class Response<T> {

    private int status;
    private String message;
    private T responseEntity;
    
    /**
     * Zero-argument constructor for Response
     */
    public Response() {
      // Zero-argument constructor is empty
    }

    /**
     * Constructor for class
     * 
     * @param status HTTP status code
     * @param message HTTP status message
     * @param responseEntity unmarshalled response entity
     */
    public Response(int status, String message, T responseEntity) {
      this.status = status; 
      this.responseEntity = responseEntity;
      this.message = message;
    }

    public T getResponseEntity() {
      return responseEntity;
    }
    
    public void setResponseEntity(T responseEntity) {
      this.responseEntity = responseEntity;
    }
    
    public int getStatus() {
      return status;
    }
    
    public void setStatus(int status) {
      this.status = status;
    }
    
    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
    
    @JsonIgnore    
    public boolean isOk() {
      return status >= 200 && status <= 299;
    }
  }
  
  /**
   * Wrapper class for ResultType<T>
   * 
   * @author Antti Leppä
   * @author Heikki Kurhinen
   *
   * @param <T> wrapped type
   */
  public static class ResultTypeWrapper <T> extends ResultType<T> {

    private Type wrappedType;
    
    /**
     * Constructor
     * 
     * @param wrappedType wrapped type
     */
    public ResultTypeWrapper(Type wrappedType) {
      this.wrappedType = wrappedType;
    }
    
    @Override
    public Type getType() {
      return wrappedType;
    }
  }
  
  /**
   * Wrapper class for ResultType<Response<T>>
   * 
   * @author Antti Leppä
   * @author Heikki Kurhinen
   *
   * @param <T> wrapped type
   */
  public static class ResponseResultTypeWrapper <T> extends ResultType<Response<T>> {
    
    private Type wrappedType;
    
    /**
     * Constructor
     * 
     * @param wrappedType wrapped type
     */
    public ResponseResultTypeWrapper(Type wrappedType) {
      this.wrappedType = wrappedType;
    }
    
    @Override
    public Type getType() {
      return new ParameterizedType() {
        
        @Override
        public Type getRawType() {
          return Response.class;
        }
        
        @Override
        public Type getOwnerType() {
          return Response.class;
        }
        
        @Override
        public Type[] getActualTypeArguments() {
          return new Type[] { wrappedType };
        }
      };
    }
  }
  
}
