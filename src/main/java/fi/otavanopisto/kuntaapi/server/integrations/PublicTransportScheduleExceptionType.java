package fi.otavanopisto.kuntaapi.server.integrations;

/**
 * Enumeration that describes exception type in public transport schedule
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public enum PublicTransportScheduleExceptionType {
  
  /**
   *  Route is exceptionally operated on date.
   */
  ADD,
  
  /**
   *  Route is exceptionally not operated on date.
   */
  REMOVE
  
}
