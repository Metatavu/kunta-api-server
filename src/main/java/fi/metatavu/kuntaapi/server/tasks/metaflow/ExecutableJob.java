package fi.metatavu.kuntaapi.server.tasks.metaflow;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Interface for describing executable jobs
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public interface ExecutableJob<T extends Task> extends Runnable {

  /**
   * Executes scheduled job 
   */
  public void execute(T task);

}