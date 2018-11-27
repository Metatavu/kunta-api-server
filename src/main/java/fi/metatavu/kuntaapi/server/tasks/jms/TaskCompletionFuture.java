package fi.metatavu.kuntaapi.server.tasks.jms;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Future implementation for task completion
 * 
 * @author Antti Leppä
 */
public class TaskCompletionFuture implements Future<Task> {
  
  private Task task;
  private boolean done;
  private Exception exception;
  
  /**
   * Constructor
   * 
   * @param task task
   */
  public TaskCompletionFuture(Task task) {
    this.task = task;
    this.done = false;
    this.exception = null;
  }

  /**
   * Marks future as complete
   */
  public void complete() {
    this.done = true;
  }

  /**
   * Marks future as failed.
   * 
   * @param exception exception
   */
  public void fail(Exception exception) {
    this.exception = exception;
    this.done = true;
  }
  
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public Task get() throws InterruptedException, ExecutionException {
    do {
      Thread.sleep(1000);
    } while (!done);
    
    if (this.exception != null) {
      throw new ExecutionException(exception);
    }
    
    return task;
  }

  @Override
  public Task get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    long end = System.currentTimeMillis() + unit.toMillis(timeout);
    
    do {
      Thread.sleep(1000);
      if (System.currentTimeMillis() > end) {
        throw new TimeoutException();
      }
    } while (!done);
    
    if (this.exception != null) {
      throw new ExecutionException(exception);
    }
    
    return task;
  }
    
}
