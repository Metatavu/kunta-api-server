package fi.metatavu.kuntaapi.server.tasks;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.TaskController;
import fi.metatavu.metaflow.tasks.AbstractTaskQueue;
import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for all Kunta API task queues
 */
public abstract class AbstractKuntaApiTaskQueue<T extends Task> extends AbstractTaskQueue<T> {

  @Inject
  private TaskController taskController;
  
  /**
   * Returns whether local node is responsible of the queue and whether the queue has any items
   * 
   * @return whether local node is responsible of the queue and whether the queue has any items
   */
  public boolean isEmptyAndLocalNodeResponsible() {
    return taskController.isEmptyAndLocalNodeResponsible(getName());
  }
  
}