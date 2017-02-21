package fi.otavanopisto.kuntaapi.server.tasks;

public class TaskQueueStatistics {
  
  private String name;
  private long tasksExecuted;
  private long duplicatedTasks;
  private long queuedTasks;
  
  public TaskQueueStatistics() {
    // Zero-argument constructor
  }
  
  public TaskQueueStatistics(String name, long tasksExecuted, long duplicatedTasks, long queuedTasks) {
    super();
    this.name = name;
    this.tasksExecuted = tasksExecuted;
    this.duplicatedTasks = duplicatedTasks;
    this.queuedTasks = queuedTasks;
  }
  
  /**
   * Returns a name of the queue
   * 
   * @return a name of the queue
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets a name of the queue
   * 
   * @param name name of the queue
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns number of tasks executed over time
   * 
   * @return number of tasks executed over time
   */
  public long getTasksExecuted() {
    return tasksExecuted;
  }
  
  /**
   * Returns number of tasks dropped as duplicates
   * 
   * @return number of tasks dropped as duplicates
   */
  public long getDuplicatedTasks() {
    return duplicatedTasks;
  }
  
  /**
   * Returns number of currently queued tasks
   * 
   * @return number of currently queued tasks
   */
  public long getQueuedTasks() {
    return queuedTasks;
  }
  
}
