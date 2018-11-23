package fi.metatavu.kuntaapi.server.tasks.metaflow;

/**
 * Base interface for scheduled jobs
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public interface Job extends Runnable {

  /**
   * Executes scheduled job 
   */
  public void timeout();

  /**
   * Returns time in millisecods before starting the initial job execution
   * 
   * @return time in millisecods before starting the initial job execution
   */
  public long getTimerWarmup();

  /**
   * Returns delay in millisecods between job executions 
   * 
   * @return delay in millisecods between job executions
   */
  public long getTimerInterval();
  
  /**
   * Returns job's name
   * 
   * @return job's name
   */
  public String getName();

}