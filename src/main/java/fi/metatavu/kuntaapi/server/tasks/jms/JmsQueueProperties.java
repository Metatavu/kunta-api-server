package fi.metatavu.kuntaapi.server.tasks.jms;

/**
 * JMS queue properties
 * 
 * @author Antti Leppä
 */
public class JmsQueueProperties {

  public static final String MAX_SESSIONS = "maxSessions";
  public static final String DESTINATION_LOOKUP = "destinationLookup";
  public static final String NO_CONCURRENCY_POOL = "no-concurrency-pool";
  public static final String LOW_CONCURRENCY_POOL = "low-concurrency-pool";
  public static final String MEDIUM_CONCURRENCY_POOL = "medium-concurrency-pool";
  public static final String HIGH_CONCURRENCY_POOL = "high-concurrency-pool";
  
  private JmsQueueProperties() {
    // Private constructor
  }

}
