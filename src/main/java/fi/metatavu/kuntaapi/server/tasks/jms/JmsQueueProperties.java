package fi.metatavu.kuntaapi.server.tasks.jms;

/**
 * JMS queue properties
 * 
 * @author Antti Leppä
 */
public class JmsQueueProperties {

  public static final String MAX_SESSIONS = "maxSessions";
  public static final String DESTINATION_LOOKUP = "destinationLookup";
  
  private JmsQueueProperties() {
    // Private constructor
  }

}
