package fi.metatavu.kuntaapi.server.tasks.jms;

import java.io.Serializable;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for responding JMS jobs
 * 
 * @author Antti Leppä
 *
 * @param <T> task
 */
public abstract class AbstractRespondingJmsJob <T extends Task, R extends Serializable> extends AbstractJmsJob<T> {
  
  /**
   * Executes scheduled job with response
   */
  public abstract R executeWithResponse(T task);
  
  /**
   * Executes scheduled job 
   */
  @Override
  public void execute(T task) {
    executeWithResponse(task);
  }
  
  @Override
  protected R executeInternal(T task) {
    return executeWithResponse(task);
  }
  
}
