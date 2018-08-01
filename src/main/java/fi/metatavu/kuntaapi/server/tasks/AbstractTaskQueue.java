package fi.metatavu.kuntaapi.server.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.ClusterController;
import fi.metatavu.kuntaapi.server.controllers.TaskController;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

/**
 * Abstract base class for all task queues
 * 
 * @author Antti Leppä
 *
 * @param <T> task type
 */
public abstract class AbstractTaskQueue <T extends AbstractTask> {
  
  @Inject
  private ClusterController clusterController;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private TaskController taskController;
  
  @Inject
  private Logger logger;
  
  private boolean running;
  
  @PostConstruct
  public void postConstruct() {
    if (taskController.findTaskQueueByName(getName()) == null) {
      taskController.createTaskQueue(getName());
    }
    
    running = true;
  }

  @PreDestroy
  public void preDestroy() {
    running = false;
  }

  /**
   * Returns unique name for task queue
   * 
   * @return unique name for task queue
   */
  public abstract String getName();
  
  /**
   * Returns next task or null if queue is empty
   * 
   * @return next task or null if queue is empty
   */
  public T next() {
    if (!running) {
      return null;
    }
    
    if (systemSettingController.isNotTestingOrTestRunning()) {
      return taskController.getNextTask(getName(), clusterController.getLocalNodeName());
    }
    
    return null;
  }

  /**
   * Enqueus new task to the queue. If priority flag is true, the task will be prepended to 
   * the front of the queue otherwise the task will be appened to the end of the queue
   * 
   * @param priority whether the task is a priority task or not
   * @param task taks
   */
  public void enqueueTask(boolean priority, T task) {
    if (!running) {
      return;
    }
    
    if (priority) {
      logger.log(Level.INFO, () -> String.format("Added priority task to queue %s", getName())); 
    }
    
    taskController.createTask(getName(), priority, task);
  }

  /**
   * Returns true if current queue is empty and local node is responsible from the queue
   * 
   * @return true if current queue is empty and local node is responsible from the queue
   */
  public boolean isEmptyAndLocalNodeResponsible() {
    return taskController.isQueueEmpty(getName()) && taskController.isNodeResponsibleFromQueue(getName(), clusterController.getLocalNodeName());
  }
  
  /**
   * Stops task queue
   */
  public void stop() {
    running = false;
  }
  
}
