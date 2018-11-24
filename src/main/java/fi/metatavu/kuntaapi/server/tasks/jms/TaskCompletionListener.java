package fi.metatavu.kuntaapi.server.tasks.jms;

import javax.jms.CompletionListener;
import javax.jms.Message;

/**
 * Event listener for listening JMS task completion
 * 
 * @author Antti Leppä
 */
public class TaskCompletionListener implements CompletionListener {
  
  private TaskCompletionFuture future;
  
  /**
   * Constructor
   * 
   * @param future task completion future
   */
  public TaskCompletionListener(TaskCompletionFuture future) {
    this.future = future;
  }

  @Override
  public void onCompletion(Message message) {
    future.complete();
  }

  @Override
  public void onException(Message message, Exception exception) {
    future.fail(exception);
  }
  
}
