package fi.otavanopisto.kuntaapi.server.tasks;

/**
 * Task request event data
 * 
 * @author Antti Leppä
 */
public class TaskRequest {

  private boolean priority;
  private AbstractTask task;

  /**
   * Constructor for TaskRequest
   * 
   * @param priority whether the task is a priority task
   * @param task task
   */
  public TaskRequest(boolean priority, AbstractTask task) {
    super();
    this.priority = priority;
    this.task = task;
  }
  
  /**
   * Returns whether the task is a priority task
   * 
   * @return whether the task is a priority task
   */
  public boolean isPriority() {
    return priority;
  }
  
  /**
   * Returns task
   * 
   * @return task
   */
  public AbstractTask getTask() {
    return task;
  }
  
}
