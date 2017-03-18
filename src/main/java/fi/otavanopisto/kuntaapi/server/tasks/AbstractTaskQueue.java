package fi.otavanopisto.kuntaapi.server.tasks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.ClusterController;
import fi.otavanopisto.kuntaapi.server.controllers.TaskController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

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
  
  private boolean running;
  
  @PostConstruct
  public void postConstruct() {
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
    
    taskController.createTask(getName(), priority, task);
  }

  /**
   * Returns whether current node is allowed to add tasks to this queue
   * 
   * @return true if node is allowed to add tasks, false otherwise
   */
  public boolean isAllowedToEnqueTasks() {
    if (!taskController.isQueueExisting(getName())) {
      return true;
    }
    
    return taskController.isNodeResponsibleFromQueue(getName(), clusterController.getLocalNodeName());
  }
  
  /**
   * Stops task queue
   */
  public void stop() {
    running = false;
  }
  
}
