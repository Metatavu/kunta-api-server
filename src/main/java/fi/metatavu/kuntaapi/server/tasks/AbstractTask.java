package fi.metatavu.kuntaapi.server.tasks;

import java.io.Serializable;

/**
 * Abstract base class for all tasks
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public abstract class AbstractTask implements Serializable {
  
  private static final long serialVersionUID = -4491072590312899600L;
  
  /**
   * Returns unique id for the task in the queue. Property is used to ensure that task is added only once to the queue
   * 
   * @return unique id
   */
  public abstract String getUniqueId();
  
}
