package fi.metatavu.kuntaapi.server.tasks.metaflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.TaskController;
import fi.metatavu.kuntaapi.server.locking.ClusterLockController;
import fi.metatavu.metaflow.tasks.AbstractTaskQueue;
import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for all Kunta API task queues
 */
public abstract class AbstractKuntaApiTaskQueue<T extends Task> extends AbstractTaskQueue<T> {

  @Inject
  private Logger logger;

  @Inject
  private TaskController taskController;

  @Inject
  private ClusterLockController clusterLockController;
  
  @Override
  public void enqueueTask(T task) {
    String lockKey = String.format("task-%s", task.getUniqueId());
    
    if (!clusterLockController.lockUntilTransactionCompletion(lockKey)) {
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.FINEST, String.format("Reroll task %s", lockKey));
      }
      
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING, "Lock reroll cooldown interrupted", e);
        }
      }
      
      enqueueTask(task);
    } else if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, String.format("Locked task %s", lockKey));
      super.enqueueTask(task);
    }
  }
  
  /**
   * Returns whether local node is responsible of the queue and whether the queue has any items
   * 
   * @return whether local node is responsible of the queue and whether the queue has any items
   */
  public boolean isEmptyAndLocalNodeResponsible() {
    return taskController.isEmptyAndLocalNodeResponsible(getName());
  }
  
}