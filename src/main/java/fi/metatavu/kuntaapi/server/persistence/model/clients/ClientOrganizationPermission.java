package fi.metatavu.kuntaapi.server.persistence.model.clients;

/**
 * Enum that describes an organization permission for a client
 * 
 * @author Antti Leppä
 *
 */
public enum ClientOrganizationPermission {
  
  /**
   * Client can be used to update services
   */
  UPDATE_SERVICES,

  /**
   * Client can be used to update service channels
   */
  UPDATE_SERVICE_CHANNELS
  
}
