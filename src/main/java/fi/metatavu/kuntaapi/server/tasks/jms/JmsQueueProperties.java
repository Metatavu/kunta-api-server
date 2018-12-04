package fi.metatavu.kuntaapi.server.tasks.jms;

/**
 * JMS queue properties
 * 
 * @author Antti Leppä
 */
public class JmsQueueProperties {

  public static final String DESTINATION_LOOKUP = "destinationLookup";
  public static final String NO_CONCURRENCY_POOL = "mdb-no-concurrency-pool";
  public static final String LOW_CONCURRENCY_POOL = "mdb-low-concurrency-pool";
  public static final String MEDIUM_CONCURRENCY_POOL = "mdb-medium-concurrency-pool";
  public static final String HIGH_CONCURRENCY_POOL = "mdb-high-concurrency-pool";
  
  private JmsQueueProperties() {
    // Private constructor
  }

}
