package fi.otavanopisto.kuntaapi.server.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import fi.otavanopisto.kuntaapi.server.controllers.TaskController;
import fi.otavanopisto.kuntaapi.server.persistence.model.Task;

/**
 * Task queue task listener.
 * 
 * This creates a Future instanse that resolves when then task completes. 
 * Listening initiates when the get method is called, so if method is never
 * invoked this won't create any stress on the system

 * @author Antti Leppä
 */
@ApplicationScoped
public class TaskListener {
  
  @Inject
  private TaskController taskController;

  /**
   * Start listening the task
   * 
   * @param task task
   * @return future representing task state
   */
  public Future<Long> listen(Task task) {
    if (task == null) {
      return null;
    }
    
    return new TaskFuture(task.getId());
  }

  @Transactional (TxType.REQUIRES_NEW)
  public boolean isCompleted(Long taskId) {
    return !taskController.isTaskExisting(taskId);
  }
  
  private class TaskFuture implements Future<Long> {
    
    private Long taskId;
    private boolean done;
    private boolean canceled;

    public TaskFuture(Long taskId) {
      this.taskId = taskId;
      this.done = false;
      this.canceled = false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      canceled = true;
      return true;
    }

    @Override
    public boolean isCancelled() {
      return canceled;
    }

    @Override
    public boolean isDone() {
      if (done) {
        return true;
      }
      
      done = isCompleted(taskId);
      
      return done;
    }

    @Override
    public Long get() throws InterruptedException, ExecutionException {
      try {
        return get(5l, TimeUnit.MINUTES);
      } catch (TimeoutException e) {
        throw new ExecutionException(e);
      }
    }
    
    @Override
    public Long get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      long started = System.currentTimeMillis();
      long timeoutTime = started + TimeUnit.MILLISECONDS.convert(timeout, unit);
      
      while (true) {
        if (isCancelled()) {
          return null;
        }
        
        if (isDone()) {
          return taskId;
        }
        
        Thread.sleep(100);
        
        if (System.currentTimeMillis() > timeoutTime) {
          cancel(true);
          throw new TimeoutException();
        }
      }
    }
    
  }
  
}
