package fi.metatavu.kuntaapi.server.persistence.model.clients;

/**
 * Enumeration that describes clients access type
 * 
 * @author Antti Leppä
 */
public enum AccessType {

  
  /**
   * Client is allowed to make GET calls
   */
  
  READ_ONLY,
  
  /**
   * Client is allowed to make GET, PUT, POST and DELETE calls
   */
  
  READ_WRITE,
  
  /**
   * Client's access to API is unrestricted (i.e. can make administrative calls)
   */
  UNRESTRICTED
  
}
