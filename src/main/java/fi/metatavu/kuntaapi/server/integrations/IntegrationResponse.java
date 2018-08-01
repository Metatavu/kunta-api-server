package fi.metatavu.kuntaapi.server.integrations;

/**
 * Class representing a reponse from integrated service
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 *
 * @param <T> successfull response entity
 */
public class IntegrationResponse<T> {

  private int status;
  private String message;
  private T entity;
  
  private IntegrationResponse(int status, T entity, String message) {
    this.status = status;
    this.entity = entity;
    this.message = message;
  }
  
  /**
   * Returns an entity when integration responded with ok
   * 
   * @return  an entity when integration responded with ok
   */
  public T getEntity() {
    return entity;
  }
  
  /**
   * Returns an error message
   * 
   * @return an error message
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * Returns HTTP status code
   * 
   * @return HTTP status code
   */
  public int getStatus() {
    return status;
  }
  
  /**
   * Returns whether the request was ok or not
   * 
   * @return whether the request was ok or not
   */
  public boolean isOk() {
    return status >= 200 && status <= 299;
  }
  
  /**
   * Constructs successful reponse
   * 
   * @param entity response entity
   * @return successful reponse
   */
  public static <T> IntegrationResponse<T> ok(T entity) {
    return new IntegrationResponse<>(200, entity, null);
  }
  
  /**
   * Constructs no content response
   * 
   * @return no content response
   */
  public static <T> IntegrationResponse<T> noContent() {
    return new IntegrationResponse<>(204, null, null);
  }
  
  /**
   * Constructs response with status and message
   * 
   * @param status http status
   * @param message response message
   * @return response with status and message
   */
  public static <T> IntegrationResponse<T> statusMessage(int status, String message) {
    return new IntegrationResponse<>(status, null, message);
  }
  
}
